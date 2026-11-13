package com.gestionclubes.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionclubes.models.EstadoSolicitud;
import com.gestionclubes.models.Mensaje;
import com.gestionclubes.models.SolicitudAmistad;
import com.gestionclubes.models.Usuario;
import com.gestionclubes.repository.MensajeRepository;
import com.gestionclubes.repository.SolicitudAmistadRepository;
import com.gestionclubes.repository.UsuarioRepository;

@Service
public class MensajeService {
    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudAmistadRepository solicitudRepository;

    public MensajeService(MensajeRepository mensajeRepository,
                          UsuarioRepository usuarioRepository,
                          SolicitudAmistadRepository solicitudRepository) {
        this.mensajeRepository = mensajeRepository;
        this.usuarioRepository = usuarioRepository;
        this.solicitudRepository = solicitudRepository;
    }

    @Transactional
    public List<Mensaje> conversacion(String email, Long amigoId) {
        Usuario usuario = usuario(email);
        validarAmistad(usuario.getId(), amigoId);
        List<Mensaje> mensajes = mensajeRepository.conversacion(usuario.getId(), amigoId);
        mensajes.stream()
                .filter(mensaje -> mensaje.getDestinatario().getId().equals(usuario.getId()))
                .forEach(mensaje -> mensaje.setLeido(true));
        return mensajes;
    }

    @Transactional(readOnly = true)
    public List<Mensaje> noLeidos(String email) {
        return mensajeRepository.noLeidos(email);
    }

    @Transactional
    public void enviar(String email, Long amigoId, String contenido) {
        Usuario remitente = usuario(email);
        if (contenido == null || contenido.isBlank() || contenido.length() > 1000) {
            throw new IllegalArgumentException("El mensaje debe tener entre 1 y 1000 caracteres");
        }
        validarAmistad(remitente.getId(), amigoId);
        Usuario destinatario = usuarioRepository.findById(amigoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el usuario seleccionado"));
        Mensaje mensaje = new Mensaje();
        mensaje.setRemitente(remitente);
        mensaje.setDestinatario(destinatario);
        mensaje.setContenido(contenido.trim());
        mensajeRepository.save(mensaje);
    }

    private Usuario usuario(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private void validarAmistad(Long primero, Long segundo) {
        SolicitudAmistad solicitud = solicitudRepository.buscarEntre(primero, segundo)
                .orElseThrow(() -> new IllegalArgumentException("Solo puedes chatear con amistades aceptadas"));
        if (solicitud.getEstado() != EstadoSolicitud.APROBADA) {
            throw new IllegalArgumentException("Solo puedes chatear con amistades aceptadas");
        }
    }
}
