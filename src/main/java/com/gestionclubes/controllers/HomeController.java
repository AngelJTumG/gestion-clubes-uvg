
package com.gestionclubes.controllers;

import com.gestionclubes.service.ClubService;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ClubService clubService;

    public HomeController(ClubService clubService) {
        this.clubService = clubService;
    }

    @GetMapping("/")
    String inicio() {
        return "index";
    }

    @GetMapping("/panel")
    String panel(Authentication authentication, Model model) {
        boolean puedeCrearClub = authentication.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("puedeCrearClub", puedeCrearClub);

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
