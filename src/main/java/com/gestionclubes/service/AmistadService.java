package com.gestionclubes.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionclubes.models.EstadoSolicitud;
import com.gestionclubes.models.SolicitudAmistad;
import com.gestionclubes.models.Usuario;
import com.gestionclubes.repository.MembresiaRepository;
import com.gestionclubes.repository.SolicitudAmistadRepository;
import com.gestionclubes.repository.UsuarioRepository;

@Service
public class AmistadService {
    private final SolicitudAmistadRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final MembresiaRepository membresiaRepository;

    public AmistadService(SolicitudAmistadRepository solicitudRepository,
                          UsuarioRepository usuarioRepository,
                          MembresiaRepository membresiaRepository) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.membresiaRepository = membresiaRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> usuariosDeClubes(String email) {
        return membresiaRepository.findUsuariosDeClubesExcepto(email);
    }

    @Transactional(readOnly = true)
    public List<SolicitudAmistad> recibidas(String email) {
        return solicitudRepository.findByDestinatario_EmailIgnoreCaseOrderByCreadaEnDesc(email);
    }

    @Transactional(readOnly = true)
    public List<SolicitudAmistad> solicitudesPendientesRecibidas(String email) {
        return solicitudRepository.findByDestinatario_EmailIgnoreCaseAndEstadoOrderByCreadaEnDesc(
                email, EstadoSolicitud.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public List<SolicitudAmistad> notificacionesRecibidas(String email) {
        return solicitudesPendientesRecibidas(email).stream()
                .filter(solicitud -> !solicitud.isLeida())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudAmistad> notificaciones(String email) {
        return solicitudRepository.findBySolicitante_EmailIgnoreCaseAndEstadoOrderByCreadaEnDesc(
                email, EstadoSolicitud.APROBADA).stream()
                .filter(solicitud -> !solicitud.isLeida())
                .toList();
    }

    @Transactional
    public void marcarNotificacionesLeidas(String email) {
        solicitudRepository.findBySolicitante_EmailIgnoreCaseAndEstadoOrderByCreadaEnDesc(
                email, EstadoSolicitud.APROBADA).forEach(solicitud -> solicitud.setLeida(true));
    }

    @Transactional
    public void marcarSolicitudesRecibidasLeidas(String email) {
        solicitudesPendientesRecibidas(email).forEach(solicitud -> solicitud.setLeida(true));
    }

    @Transactional
    public void solicitar(String emailSolicitante, Long destinatarioId) {
        Usuario solicitante = buscarUsuario(emailSolicitante);
        Usuario destinatario = usuarioRepository.findById(destinatarioId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el usuario seleccionado"));
        if (solicitante.getId().equals(destinatario.getId())) {
            throw new IllegalArgumentException("No puedes enviarte una solicitud a ti mismo");
        }
        if (!membresiaRepository.existsByEstudiante_Usuario_EmailIgnoreCase(destinatario.getEmail())) {
            throw new IllegalArgumentException("Solo puedes conectar con usuarios que pertenecen a un club");
        }
        SolicitudAmistad existente = solicitudRepository.buscarEntre(solicitante.getId(), destinatario.getId())
                .orElse(null);
        if (existente != null) {
            throw new IllegalArgumentException("Ya existe una solicitud o amistad con este usuario");
        }
        SolicitudAmistad solicitud = new SolicitudAmistad();
        solicitud.setSolicitante(solicitante);
        solicitud.setDestinatario(destinatario);
        solicitudRepository.save(solicitud);
    }

    @Transactional
    public void responder(Long id, String email, boolean aprobar) {
        SolicitudAmistad solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la solicitud de amistad"));
        if (!solicitud.getDestinatario().getEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("No tienes permiso para responder esta solicitud");
        }
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalArgumentException("La solicitud ya fue atendida");
        }
        solicitud.setEstado(aprobar ? EstadoSolicitud.APROBADA : EstadoSolicitud.RECHAZADA);
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }
}
