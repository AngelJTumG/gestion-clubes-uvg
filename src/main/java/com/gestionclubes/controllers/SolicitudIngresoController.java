package com.gestionclubes.controllers;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionclubes.models.Rol;
import com.gestionclubes.service.SolicitudIngresoService;
import com.gestionclubes.service.SolicitudIngresoService.ResultadoAprobacionMasiva;

@Controller
public class SolicitudIngresoController {
    private final SolicitudIngresoService solicitudService;

    public SolicitudIngresoController(SolicitudIngresoService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping("/clubes/{clubId}/carne")
    public String completarCarne(@PathVariable Long clubId, @RequestParam String carne,
                                 Principal principal, RedirectAttributes redirect) {
        try {
            solicitudService.completarCarne(principal.getName(), carne);
            redirect.addFlashAttribute("exito", "Carné registrado. Tus ingresos ya aprobados se incorporaron a los clubes.");
        } catch (DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error", "Ese carné ya está registrado. Revisa tus datos.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
        return "redirect:/admin/clubes/" + clubId;
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

    @PostMapping("/admin/solicitudes/aprobar-todas")
    public String aprobarTodas(Authentication authentication, RedirectAttributes redirect) {
        boolean administradorGlobal = authentication.getAuthorities().stream()
                .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_" + Rol.ADMIN.name()));
        try {
            ResultadoAprobacionMasiva resultado = solicitudService.aprobarTodas(
                    authentication.getName(), administradorGlobal);
            if (resultado.pendientesSinCarne() == 0) {
                redirect.addFlashAttribute("exito",
                        resultado.aprobadas() + " solicitud(es) aprobada(s) correctamente");
            } else {
                redirect.addFlashAttribute("exito",
                        resultado.aprobadas() + " solicitud(es) aprobada(s). "
                                + resultado.pendientesSinCarne()
                                + " quedaron pendientes porque no tienen carné registrado.");
            }
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
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
