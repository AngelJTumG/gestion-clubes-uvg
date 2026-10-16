
package com.gestionclubes.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gestionclubes.service.ClubService;
import com.gestionclubes.service.SolicitudIngresoService;

@Controller
public class HomeController {

    private final ClubService clubService;
    private final SolicitudIngresoService solicitudService;

    public HomeController(ClubService clubService, SolicitudIngresoService solicitudService) {
        this.clubService = clubService;
        this.solicitudService = solicitudService;
    }

    @GetMapping("/")
    String inicio() {
        return "index";
    }

    @GetMapping("/panel")
    String panel(Authentication authentication, Model model) {
        boolean puedeCrearClub = authentication.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN"));
        boolean puedeGestionarSolicitudes = authentication.getAuthorities().stream()
            .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN_CLUB")
                || rol.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("puedeCrearClub", puedeCrearClub);
        model.addAttribute("puedeGestionarSolicitudes", puedeGestionarSolicitudes);
        if (puedeGestionarSolicitudes) {
            boolean administradorGlobal = authentication.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN"));
            model.addAttribute("solicitudes",
                solicitudService.pendientesPara(authentication.getName(), administradorGlobal));
        }

        // Obtener los clubes registrados para mostrarlos en el panel
        model.addAttribute(
            "clubes",
            puedeCrearClub
                ? clubService.listarClubes()
                : clubService.listarClubesActivos()
        );

        return "panel";
    }
}
