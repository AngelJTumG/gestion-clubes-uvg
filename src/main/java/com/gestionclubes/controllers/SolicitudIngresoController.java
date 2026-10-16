package com.gestionclubes.controllers;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionclubes.models.Rol;
import com.gestionclubes.service.SolicitudIngresoService;

@Controller
public class SolicitudIngresoController {
    private final SolicitudIngresoService solicitudService;

    public SolicitudIngresoController(SolicitudIngresoService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping("/clubes/{clubId}/solicitud")
    public String solicitar(@PathVariable Long clubId, Principal principal,
                            RedirectAttributes redirect) {
        try {
            solicitudService.solicitar(clubId, principal.getName());
            redirect.addFlashAttribute("exito", "Solicitud enviada al administrador del club");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
        return "redirect:/admin/clubes/" + clubId;
    }

    @PostMapping("/club-admin/solicitudes/{solicitudId}/aprobar")
    public String aprobar(@PathVariable Long solicitudId, Authentication authentication,
                          RedirectAttributes redirect) {
        gestionar(solicitudId, authentication, redirect, true);
        return "redirect:/panel";
    }

    @PostMapping("/club-admin/solicitudes/{solicitudId}/rechazar")
    public String rechazar(@PathVariable Long solicitudId, Authentication authentication,
                           RedirectAttributes redirect) {
        gestionar(solicitudId, authentication, redirect, false);
        return "redirect:/panel";
    }

    private void gestionar(Long solicitudId, Authentication authentication,
                           RedirectAttributes redirect, boolean aprobar) {
        String email = authentication.getName();
        boolean administradorGlobal = authentication.getAuthorities().stream()
                .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_" + Rol.ADMIN.name()));
        try {
            if (aprobar) solicitudService.aprobar(solicitudId, email, administradorGlobal);
            else solicitudService.rechazar(solicitudId, email, administradorGlobal);
            redirect.addFlashAttribute("exito", aprobar ? "Solicitud aprobada" : "Solicitud rechazada");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}
