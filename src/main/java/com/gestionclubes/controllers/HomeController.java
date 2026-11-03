
package com.gestionclubes.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    String panel(Authentication authentication, Model model,
                 @RequestParam(defaultValue = "") String busqueda,
                 @RequestParam(defaultValue = "") String categoria,
                 @RequestParam(defaultValue = "todos") String estado) {
        boolean puedeCrearClub = authentication.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN"));
        boolean puedeGestionarSolicitudes = authentication.getAuthorities().stream()
            .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN_CLUB")
                || rol.getAuthority().equals("ROLE_ADMIN"));
        boolean esAdministradorGlobal = authentication.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("puedeCrearClub", puedeCrearClub);
        model.addAttribute("puedeGestionarSolicitudes", puedeGestionarSolicitudes);
        model.addAttribute("puedeAprobarTodas", esAdministradorGlobal);
        if (puedeGestionarSolicitudes) {
            model.addAttribute("solicitudes",
                solicitudService.pendientesPara(authentication.getName(), esAdministradorGlobal));
        }

        model.addAttribute("busqueda", busqueda);
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("categorias", clubService.listarCategorias(puedeCrearClub));
        model.addAttribute("clubes", clubService.filtrarClubes(
                puedeCrearClub, busqueda, categoria, estado));

        return "panel";
    }
}
