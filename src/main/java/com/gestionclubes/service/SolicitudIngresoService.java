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

    public SolicitudIngresoService(SolicitudIngresoRepository solicitudRepository,
                                   ClubRepository clubRepository,
                                   UsuarioRepository usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.clubRepository = clubRepository;
        this.usuarioRepository = usuarioRepository;
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

    @Transactional
    public void aprobar(Long solicitudId, String email, boolean administradorGlobal) {
        SolicitudIngreso solicitud = obtenerAutorizada(solicitudId, email, administradorGlobal);
        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitudRepository.save(solicitud);
    }

    @Transactional
    public void rechazar(Long solicitudId, String email, boolean administradorGlobal) {
        SolicitudIngreso solicitud = obtenerAutorizada(solicitudId, email, administradorGlobal);
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitudRepository.save(solicitud);
    }

    private SolicitudIngreso obtenerAutorizada(Long solicitudId, String email, boolean administradorGlobal) {
        SolicitudIngreso solicitud = solicitudRepository.findById(solicitudId)
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
}
