package com.gestionclubes.controllers;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.gestionclubes.models.Usuario;
import com.gestionclubes.service.UsuarioService;

@ControllerAdvice
public class GlobalModelAttributes {
    private final UsuarioService usuarioService;

    public GlobalModelAttributes(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual(Principal principal) {
        return principal == null ? null : usuarioService.buscarPorEmail(principal.getName());
    }
}
