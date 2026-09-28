package com.gestionclubes.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.gestionclubes.dtos.RegistroUsuarioDto;
import com.gestionclubes.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
public class AuthController {
    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    String login() { return "auth/login"; }

    @GetMapping("/registro")
    String registro(Model model) {
        model.addAttribute("usuario", new RegistroUsuarioDto());
        return "auth/registro";
    }

    @PostMapping("/registro")
    String registrar(@Valid @ModelAttribute("usuario") RegistroUsuarioDto dto,
                    BindingResult result) {
        if (!dto.getPassword().equals(dto.getConfirmarPassword())) {
            result.rejectValue("confirmarPassword", "password.mismatch", "Las contraseñas no coinciden");
        }
        if (result.hasErrors()) return "auth/registro";
        try {
            usuarioService.registrar(dto);
            return "redirect:/login?registroExitoso";
        } catch (IllegalArgumentException ex) {
            result.reject("registro.error", ex.getMessage());
            return "auth/registro";
        }
    }
}

