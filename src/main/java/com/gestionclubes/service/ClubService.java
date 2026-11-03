package com.gestionclubes.service;

import java.util.Locale;
import java.util.Optional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.gestionclubes.dtos.RegistroClubDto;
import com.gestionclubes.models.Club;
import com.gestionclubes.models.Coordinador;
import com.gestionclubes.models.Rol;
import com.gestionclubes.models.Usuario;
import com.gestionclubes.repository.ClubRepository;
import com.gestionclubes.repository.CoordinadorRepository;
import com.gestionclubes.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionclubes.models.Fotografia;
import com.gestionclubes.repository.FotografiaRepository;

@Service
public class ClubService {
    private final ClubRepository clubRepository;
    private final CoordinadorRepository coordinadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final FotografiaRepository fotografiaRepository;

    public ClubService(ClubRepository clubRepository,
                        CoordinadorRepository coordinadorRepository,
                        UsuarioRepository usuarioRepository,
                        FotografiaRepository fotografiaRepository) {
        this.clubRepository = clubRepository;
        this.coordinadorRepository = coordinadorRepository;
        this.usuarioRepository = usuarioRepository;
        this.fotografiaRepository = fotografiaRepository;
}

    @Transactional
    public Club crear(RegistroClubDto dto) {
        String nombre = dto.getNombre().trim();
        if (clubRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe un club con ese nombre");
        }

        String email = dto.getCoordinadorEmail().trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.buscarPorEmailParaActualizar(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una cuenta con el correo del coordinador"));

        if (!usuario.isActivo()) {
            throw new IllegalArgumentException("La cuenta del coordinador está inactiva");
        }
        if (usuario.getRol() != Rol.ADMIN_CLUB && usuario.getRol() != Rol.ADMIN) {
            throw new IllegalArgumentException(
                    "El coordinador debe tener el rol ADMIN_CLUB o ADMIN");
        }

        Coordinador coordinador = coordinadorRepository.findByUsuario_Id(usuario.getId())
                .orElseGet(() -> {
                    Coordinador nuevo = new Coordinador();
                    nuevo.setUsuario(usuario);
                    return coordinadorRepository.save(nuevo);
                });

        Club club = new Club();
        club.setNombre(nombre);
        club.setDescripcion(dto.getDescripcion().trim());
        club.setCategoria(dto.getCategoria().trim());
        club.setHorario(dto.getHorario().trim());
        club.setUbicacion(dto.getUbicacion().trim());
        club.setRequisitos(dto.getRequisitos().trim());
        club.setDatosContacto(dto.getDatosContacto().trim());
        club.setCoordinador(coordinador);

        return clubRepository.saveAndFlush(club);
    }

    @Transactional(readOnly = true)
    public Optional<Club> buscarPorId(Long id) {
        return clubRepository.findOneById(id);
    }

    @Transactional(readOnly = true)
    public List<Club> listarClubes() {
        return clubRepository.findAll();
    }

    @Transactional(readOnly = true)
    public RegistroClubDto obtenerFormularioEdicion(Long id) {
        Club club = obtenerClubExistente(id);
        RegistroClubDto dto = new RegistroClubDto();
        dto.setNombre(club.getNombre());
        dto.setDescripcion(club.getDescripcion());
        dto.setCategoria(club.getCategoria());
        dto.setHorario(club.getHorario());
        dto.setUbicacion(club.getUbicacion());
        dto.setRequisitos(club.getRequisitos());
        dto.setDatosContacto(club.getDatosContacto());
        dto.setCoordinadorEmail(club.getCoordinador().getUsuario().getEmail());
        return dto;
    }

    @Transactional
    public Club actualizar(Long id, RegistroClubDto dto) {
        Club club = obtenerClubExistente(id);
        String nombre = dto.getNombre().trim();
        if (clubRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new IllegalArgumentException("Ya existe otro club con ese nombre");
        }

        Coordinador coordinador = resolverCoordinador(dto.getCoordinadorEmail());
        copiarDatos(club, dto, coordinador);
        return clubRepository.saveAndFlush(club);
    }

    private Club obtenerClubExistente(Long id) {
        return buscarPorId(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe el club solicitado"));
    }

    private Coordinador resolverCoordinador(String correo) {
        String email = correo.trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.buscarPorEmailParaActualizar(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una cuenta con el correo del coordinador"));

        if (!usuario.isActivo()) {
            throw new IllegalArgumentException("La cuenta del coordinador está inactiva");
        }
        if (usuario.getRol() != Rol.ADMIN_CLUB && usuario.getRol() != Rol.ADMIN) {
            throw new IllegalArgumentException(
                    "El coordinador debe tener el rol ADMIN_CLUB o ADMIN");
        }

        return coordinadorRepository.findByUsuario_Id(usuario.getId())
                .orElseGet(() -> {
                    Coordinador nuevo = new Coordinador();
                    nuevo.setUsuario(usuario);
                    return coordinadorRepository.save(nuevo);
                });

    }

    private void copiarDatos(Club club, RegistroClubDto dto, Coordinador coordinador) {
        club.setNombre(dto.getNombre().trim());
        club.setDescripcion(dto.getDescripcion().trim());
        club.setCategoria(dto.getCategoria().trim());
        club.setHorario(dto.getHorario().trim());
        club.setUbicacion(dto.getUbicacion().trim());
        club.setRequisitos(dto.getRequisitos().trim());
        club.setDatosContacto(dto.getDatosContacto().trim());
        club.setCoordinador(coordinador);
    }

    @Transactional(readOnly = true)
    public List<Club> listarClubesActivos() {
        return clubRepository.findByStatusTrue();
    }

    @Transactional(readOnly = true)
    public List<Club> filtrarClubes(boolean incluirInactivos, String busqueda, String categoria,
                                    String estado) {
        List<Club> clubes = incluirInactivos ? listarClubes() : listarClubesActivos();
        String texto = busqueda == null ? "" : busqueda.trim().toLowerCase(Locale.ROOT);
        String categoriaNormalizada = categoria == null ? "" : categoria.trim();
        String estadoNormalizado = estado == null ? "todos" : estado.trim().toLowerCase(Locale.ROOT);

        return clubes.stream()
                .filter(club -> texto.isEmpty()
                        || club.getNombre().toLowerCase(Locale.ROOT).contains(texto))
                .filter(club -> categoriaNormalizada.isEmpty()
                        || club.getCategoria().equalsIgnoreCase(categoriaNormalizada))
                .filter(club -> !incluirInactivos
                        || estadoNormalizado.equals("todos")
                        || (estadoNormalizado.equals("activos") && club.isStatus())
                        || (estadoNormalizado.equals("inactivos") && !club.isStatus()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> listarCategorias(boolean incluirInactivos) {
        List<Club> clubes = incluirInactivos ? listarClubes() : listarClubesActivos();
        return clubes.stream()
                .map(Club::getCategoria)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(categoria -> !categoria.isEmpty())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    @Transactional
    public void desactivar(Long id) {
        Club club = buscarPorId(id)
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "No existe el club solicitado"
                )
            );
        club.setStatus(false);
        clubRepository.save(club);
    }

    @Transactional
    public void activar(Long id) {
        Club club = obtenerClubExistente(id);
        club.setStatus(true);
        clubRepository.save(club);
    }

    @Transactional(readOnly = true)
    public List<Fotografia> consultarFotografias(Long idClub) {
        obtenerClubExistente(idClub);
        return fotografiaRepository.findByClub_IdOrderByIdAsc(idClub);
}

}
