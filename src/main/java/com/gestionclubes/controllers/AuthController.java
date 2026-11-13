package com.gestionclubes.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.gestionclubes.dtos.RegistroUsuarioDto;
import com.gestionclubes.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * Atiende las páginas de inicio de sesión y registro de usuarios.
 */
@Controller
public class AuthController {
    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Muestra el formulario de inicio de sesión. */
    @GetMapping("/login")
    String login() { return "auth/login"; }

    /** Prepara el formulario y su DTO para registrar una cuenta. */
    @GetMapping("/registro")
    String registro(Model model) {
        model.addAttribute("usuario", new RegistroUsuarioDto());
        return "auth/registro";
    }

    /** Valida y registra la cuenta; si hay errores, vuelve a mostrar el formulario. */
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
        } catch (DataIntegrityViolationException ex) {
            result.reject("registro.duplicado", "El correo o carné ya está registrado. Revisa tus datos.");
            return "auth/registro";
        } catch (IllegalArgumentException ex) {
            result.reject("registro.error", ex.getMessage());
            return "auth/registro";
        }
    }
}

