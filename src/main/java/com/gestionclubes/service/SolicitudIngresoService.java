package com.gestionclubes.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionclubes.models.Club;
import com.gestionclubes.models.EstadoSolicitud;
import com.gestionclubes.models.Rol;
import com.gestionclubes.models.SolicitudIngreso;
import com.gestionclubes.models.Usuario;
import com.gestionclubes.repository.ClubRepository;
import com.gestionclubes.repository.SolicitudIngresoRepository;
import com.gestionclubes.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SolicitudIngresoService {
    private final SolicitudIngresoRepository solicitudRepository;
    private final ClubRepository clubRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstudianteService estudianteService;
    private final MembresiaService membresiaService;

    public SolicitudIngresoService(SolicitudIngresoRepository solicitudRepository,
                                   ClubRepository clubRepository,
                                   UsuarioRepository usuarioRepository,
                                   EstudianteService estudianteService,
                                   MembresiaService membresiaService) {
        this.solicitudRepository = solicitudRepository;
        this.clubRepository = clubRepository;
        this.usuarioRepository = usuarioRepository;
        this.estudianteService = estudianteService;
        this.membresiaService = membresiaService;
    }

    @Transactional
    public void solicitar(Long clubId, String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("No existe el usuario autenticado"));
        if (usuario.getRol() != Rol.USUARIO) {
            throw new IllegalArgumentException("Solo los usuarios pueden solicitar ingreso a un club");
        }

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new EntityNotFoundException("No existe el club solicitado"));
        if (!club.isStatus()) {
            throw new IllegalArgumentException("El club está inactivo");
        }

        if (!estudianteService.tieneCarne(email)) {
            throw new IllegalArgumentException("Registra tu carné antes de solicitar ingreso a un club");
        }

        Optional<SolicitudIngreso> existente = solicitudRepository
                .findByUsuario_EmailIgnoreCaseAndClub_Id(email, clubId);
        if (existente.isPresent()) {
            SolicitudIngreso solicitud = existente.get();
            if (solicitud.getEstado() == EstadoSolicitud.RECHAZADA) {
                solicitud.setEstado(EstadoSolicitud.PENDIENTE);
                solicitudRepository.save(solicitud);
                return;
            }
            throw new IllegalArgumentException("Ya existe una solicitud para este club");
        }

        SolicitudIngreso solicitud = new SolicitudIngreso();
        solicitud.setUsuario(usuario);
        solicitud.setClub(club);
        solicitudRepository.save(solicitud);
    }

    @Transactional(readOnly = true)
    public Optional<EstadoSolicitud> estadoPara(Long clubId, String email) {
        return solicitudRepository.findByUsuario_EmailIgnoreCaseAndClub_Id(email, clubId)
                .map(SolicitudIngreso::getEstado);
    }

    @Transactional(readOnly = true)
    public List<SolicitudIngreso> pendientesPara(String email, boolean administradorGlobal) {
        return administradorGlobal
                ? solicitudRepository.findByEstadoOrderByCreadaEnAsc(EstadoSolicitud.PENDIENTE)
                : solicitudRepository.findByClub_Coordinador_Usuario_EmailIgnoreCaseAndEstadoOrderByCreadaEnAsc(
                        email, EstadoSolicitud.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public List<SolicitudIngreso> solicitudesDe(String email) {
        return solicitudRepository.findByUsuario_EmailIgnoreCaseOrderByCreadaEnDesc(email);
    }

    @Transactional(readOnly = true)
    public List<SolicitudIngreso> notificacionesDe(String email) {
        return solicitudesDe(email).stream()
                .filter(solicitud -> solicitud.getEstado() != EstadoSolicitud.PENDIENTE && !solicitud.isLeida())
                .toList();
    }

    @Transactional
    public void marcarNotificacionesLeidas(String email) {
        solicitudesDe(email).stream()
                .filter(solicitud -> solicitud.getEstado() != EstadoSolicitud.PENDIENTE)
                .forEach(solicitud -> solicitud.setLeida(true));
    }

    @Transactional
    public void aprobar(Long solicitudId, String email, boolean administradorGlobal) {
        SolicitudIngreso solicitud = obtenerAutorizada(solicitudId, email, administradorGlobal);
        solicitud.setEstado(EstadoSolicitud.APROBADA);
        membresiaService.registrarAprobada(solicitud);
        solicitudRepository.save(solicitud);
    }

    @Transactional
    public void rechazar(Long solicitudId, String email, boolean administradorGlobal) {
        SolicitudIngreso solicitud = obtenerAutorizada(solicitudId, email, administradorGlobal);
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitudRepository.save(solicitud);
    }

    @Transactional
    public ResultadoAprobacionMasiva aprobarTodas(String email, boolean administradorGlobal) {
        if (!administradorGlobal) {
            throw new IllegalArgumentException("Solo el administrador general puede aprobar todas las solicitudes");
        }

        int aprobadas = 0;
        int pendientesSinCarne = 0;
        for (SolicitudIngreso pendiente : solicitudRepository.findByEstadoOrderByCreadaEnAsc(
                EstadoSolicitud.PENDIENTE)) {
            SolicitudIngreso solicitud = solicitudRepository.buscarParaGestionar(pendiente.getId())
                    .orElseThrow(() -> new EntityNotFoundException("No existe la solicitud"));
            if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
                continue;
            }
            if (!estudianteService.tieneCarne(solicitud.getUsuario().getEmail())) {
                pendientesSinCarne++;
                continue;
            }

            solicitud.setEstado(EstadoSolicitud.APROBADA);
            membresiaService.registrarAprobada(solicitud);
            solicitudRepository.save(solicitud);
            aprobadas++;
        }
        return new ResultadoAprobacionMasiva(aprobadas, pendientesSinCarne);
    }

    @Transactional
    public void completarCarne(String email, String carne) {
        Usuario usuario = usuarioRepository.buscarPorEmailParaActualizar(email)
                .orElseThrow(() -> new EntityNotFoundException("No existe el usuario autenticado"));
        if (usuario.getRol() != Rol.USUARIO) {
            throw new IllegalArgumentException("Solo las cuentas de estudiantes pueden registrar su carné aquí");
        }
        estudianteService.registrar(usuario, carne);
        membresiaService.sincronizarAprobadasDe(usuario);
    }

    private SolicitudIngreso obtenerAutorizada(Long solicitudId, String email, boolean administradorGlobal) {
        SolicitudIngreso solicitud = solicitudRepository.buscarParaGestionar(solicitudId)
                .orElseThrow(() -> new EntityNotFoundException("No existe la solicitud"));
        boolean esCoordinador = solicitud.getClub().getCoordinador().getUsuario().getEmail().equalsIgnoreCase(email);
        if (!administradorGlobal && !esCoordinador) {
            throw new IllegalArgumentException("No tienes permiso para gestionar esta solicitud");
        }
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalArgumentException("La solicitud ya fue gestionada");
        }
        return solicitud;
    }

    public record ResultadoAprobacionMasiva(int aprobadas, int pendientesSinCarne) {
    }
}
