package com.gestionclubes.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestionclubes.models.EstadoSolicitud;
import com.gestionclubes.models.Estudiante;
import com.gestionclubes.models.Membresia;
import com.gestionclubes.models.SolicitudIngreso;
import com.gestionclubes.models.Usuario;
import com.gestionclubes.repository.EstudianteRepository;
import com.gestionclubes.repository.MembresiaRepository;
import com.gestionclubes.repository.SolicitudIngresoRepository;
import com.gestionclubes.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class MembresiaService {
    private final MembresiaRepository membresiaRepository;
    private final EstudianteRepository estudianteRepository;
    private final SolicitudIngresoRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    public MembresiaService(MembresiaRepository membresiaRepository,
                           EstudianteRepository estudianteRepository,
                           SolicitudIngresoRepository solicitudRepository,
                           UsuarioRepository usuarioRepository) {
        this.membresiaRepository = membresiaRepository;
        this.estudianteRepository = estudianteRepository;
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Membresia> listarPorClub(Long clubId) {
        return membresiaRepository.findByClub_IdOrderByFechaIngresoAsc(clubId);
    }

    @Transactional(readOnly = true)
    public List<Membresia> listarPorEstudiante(String email) {
        return membresiaRepository.findByEstudiante_Usuario_EmailIgnoreCaseOrderByFechaIngresoAsc(email);
    }

    @Transactional(readOnly = true)
    public boolean esMiembro(String email, Long clubId) {
        return membresiaRepository.findByEstudiante_Usuario_EmailIgnoreCaseAndClub_Id(email, clubId)
                .isPresent();
    }

    @Transactional
    public void salir(String email, Long clubId) {
        Membresia membresia = membresiaRepository
                .findByEstudiante_Usuario_EmailIgnoreCaseAndClub_Id(email, clubId)
                .orElseThrow(() -> new IllegalArgumentException("No perteneces a este club"));
        eliminarYPermitirNuevaSolicitud(membresia);
    }

    @Transactional
    public void retirar(Long membresiaId, Long clubId) {
        Membresia membresia = membresiaRepository.findById(membresiaId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la membresía seleccionada"));
        if (!membresia.getClub().getId().equals(clubId)) {
            throw new IllegalArgumentException("La membresía no pertenece a este club");
        }
        eliminarYPermitirNuevaSolicitud(membresia);
    }

    private void eliminarYPermitirNuevaSolicitud(Membresia membresia) {
        solicitudRepository.findByUsuario_EmailIgnoreCaseAndClub_Id(
                membresia.getEstudiante().getUsuario().getEmail(), membresia.getClub().getId())
                .ifPresent(solicitud -> {
                    solicitud.setEstado(EstadoSolicitud.RECHAZADA);
                    solicitud.setLeida(false);
                    solicitudRepository.save(solicitud);
                });
        membresiaRepository.delete(membresia);
    }

    @Transactional
    public void registrarAprobada(SolicitudIngreso solicitud) {
        if (solicitud.getEstado() != EstadoSolicitud.APROBADA) {
            throw new IllegalArgumentException("Solo una solicitud aprobada puede crear una membresía");
        }
        Usuario usuario = usuarioRepository.buscarPorEmailParaActualizar(solicitud.getUsuario().getEmail())
                .orElseThrow(() -> new EntityNotFoundException("No existe el usuario solicitante"));
        Estudiante estudiante = estudianteRepository.findByUsuario_Id(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El solicitante debe registrar su carné desde el detalle del club antes de aprobar"));
        registrarSiFalta(solicitud, estudiante);
    }

    private void registrarSiFalta(SolicitudIngreso solicitud, Estudiante estudiante) {
        if (membresiaRepository.findByEstudiante_CarneAndClub_Id(
            estudiante.getCarne(), solicitud.getClub().getId()).isEmpty()) {
            membresiaRepository.save(solicitud.getClub().agregarIntegrante(estudiante, LocalDate.now()));
        }
    }

    @Transactional
    public void sincronizarAprobadasDe(Usuario usuario) {
        for (SolicitudIngreso solicitud : solicitudRepository.findByUsuario_IdAndEstado(
                usuario.getId(), EstadoSolicitud.APROBADA)) {
            registrarAprobada(solicitud);
        }
    }

    @Transactional
    public int sincronizarAprobadas() {
        int sinCarne = 0;
        for (SolicitudIngreso solicitud : solicitudRepository.findByEstadoOrderByCreadaEnAsc(
                EstadoSolicitud.APROBADA)) {
            if (estudianteRepository.findByUsuario_Id(solicitud.getUsuario().getId()).isPresent()) {
                registrarAprobada(solicitud);
            } else {
                sinCarne++;
            }
        }
        return sinCarne;
    }
}
