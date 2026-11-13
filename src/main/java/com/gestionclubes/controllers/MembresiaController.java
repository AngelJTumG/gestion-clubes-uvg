package com.gestionclubes.controllers;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionclubes.service.MembresiaService;

@Controller
public class MembresiaController {
    private final MembresiaService membresiaService;

    public MembresiaController(MembresiaService membresiaService) {
        this.membresiaService = membresiaService;
    }

    @PostMapping("/clubes/{clubId}/salir")
    public String salir(@PathVariable Long clubId, Principal principal,
                        RedirectAttributes redirect) {
        try {
            membresiaService.salir(principal.getName(), clubId);
            redirect.addFlashAttribute("exito", "Has salido del club correctamente");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/panel";
    }
}
