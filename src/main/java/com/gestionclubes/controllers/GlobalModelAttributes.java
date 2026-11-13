package com.gestionclubes.controllers;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.gestionclubes.models.Usuario;
import com.gestionclubes.service.UsuarioService;
import com.gestionclubes.service.MensajeService;

/**
 * Agrega atributos compartidos a los modelos de las vistas.
 */
@ControllerAdvice
public class GlobalModelAttributes {
    private final UsuarioService usuarioService;
    private final MensajeService mensajeService;

    public GlobalModelAttributes(UsuarioService usuarioService, MensajeService mensajeService) {
        this.usuarioService = usuarioService;
        this.mensajeService = mensajeService;
    }

    /** Agrega el usuario autenticado a cada vista, o null si no hay sesión. */
    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual(Principal principal) {
        return principal == null ? null : usuarioService.buscarPorEmail(principal.getName());
    }

    @ModelAttribute("mensajesNoLeidos")
    public java.util.List<com.gestionclubes.models.Mensaje> mensajesNoLeidos(Principal principal) {
        return principal == null ? java.util.List.of() : mensajeService.noLeidos(principal.getName());
    }
}
