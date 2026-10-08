package com.gestionclubes.service;

import java.util.Locale;
import java.util.Optional;

import com.gestionclubes.dtos.RegistroClubDto;
import com.gestionclubes.models.Club;
import com.gestionclubes.models.Coordinador;
import com.gestionclubes.models.Rol;
import com.gestionclubes.models.Usuario;
import com.gestionclubes.repository.ClubRepository;
import com.gestionclubes.repository.CoordinadorRepository;
import com.gestionclubes.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClubService {
    private final ClubRepository clubRepository;
    private final CoordinadorRepository coordinadorRepository;
    private final UsuarioRepository usuarioRepository;

    public ClubService(ClubRepository clubRepository,
                       CoordinadorRepository coordinadorRepository,
                       UsuarioRepository usuarioRepository) {
        this.clubRepository = clubRepository;
        this.coordinadorRepository = coordinadorRepository;
        this.usuarioRepository = usuarioRepository;
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
}
