package com.gestionclubes.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    String inicio() { return "index"; }

    @GetMapping("/panel")
    String panel(Authentication authentication, Model model) {
        boolean puedeCrearClub = authentication.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("puedeCrearClub", puedeCrearClub);
        return "panel";
    }
}

