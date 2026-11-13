package com.gestionclubes.controllers;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionclubes.service.AmistadService;

/**
 * Gestiona la lista de personas y las solicitudes de amistad.
 */
@Controller
public class SocialController {
    private final AmistadService amistadService;

    public SocialController(AmistadService amistadService) {
        this.amistadService = amistadService;
    }

    /** Muestra usuarios de clubes y solicitudes de amistad recibidas. */
    @GetMapping("/personas")
    public String personas(Principal principal, Model model) {
        model.addAttribute("usuarios", amistadService.usuariosDeClubes(principal.getName()));
        model.addAttribute("solicitudesAmistad", amistadService.recibidas(principal.getName()));
        return "personas";
    }

    /** Envía una solicitud de amistad y muestra el resultado en la lista de personas. */
    @PostMapping("/personas/{usuarioId}/solicitar")
    public String solicitar(@PathVariable Long usuarioId, Principal principal,
                            RedirectAttributes redirect) {
        try {
            amistadService.solicitar(principal.getName(), usuarioId);
            redirect.addFlashAttribute("exito", "Solicitud de amistad enviada");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/personas";
    }

    /** Acepta una solicitud de amistad recibida. */
    @PostMapping("/amistades/{id}/aprobar")
    public String aprobar(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        responder(id, principal, redirect, true);
        return "redirect:/personas";
    }

    /** Rechaza una solicitud de amistad recibida. */
    @PostMapping("/amistades/{id}/rechazar")
    public String rechazar(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        responder(id, principal, redirect, false);
        return "redirect:/personas";
    }

    /** Aplica la respuesta solicitada y agrega un mensaje de éxito o error. */
    private void responder(Long id, Principal principal, RedirectAttributes redirect, boolean aprobar) {
        try {
            amistadService.responder(id, principal.getName(), aprobar);
            redirect.addFlashAttribute("exito",
                    aprobar ? "Solicitud de amistad aceptada" : "Solicitud de amistad rechazada");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
    }
}
