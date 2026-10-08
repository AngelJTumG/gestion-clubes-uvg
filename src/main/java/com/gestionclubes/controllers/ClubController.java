package com.gestionclubes.controllers;

import java.security.Principal;

import com.gestionclubes.dtos.RegistroClubDto;
import com.gestionclubes.models.Club;
import com.gestionclubes.service.ClubService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/clubes")
public class ClubController {
    private static final Logger log = LoggerFactory.getLogger(ClubController.class);
    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @GetMapping("/nuevo")
    public String formulario(Model model, Principal principal) {
        RegistroClubDto dto = new RegistroClubDto();
        dto.setCoordinadorEmail(principal.getName());
        model.addAttribute("club", dto);
        return "clubes/nuevo";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("club") RegistroClubDto dto,
                        BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) return "clubes/nuevo";
        try {
            Club club = clubService.crear(dto);
            redirect.addFlashAttribute("exito",
                    "Club creado correctamente. Identificador: " + club.getId());
            return "redirect:/admin/clubes/" + club.getId();
        } catch (IllegalArgumentException ex) {
            result.reject("club.error", ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            log.warn("Una restricción de integridad impidió crear el club", ex);
            result.reject("club.integridad",
                    "No se pudo guardar el club. Comprueba si ya existe y vuelve a intentar.");
        } catch (TransientDataAccessException ex) {
            log.warn("Conflicto temporal al crear un club", ex);
            result.reject("club.temporal", "No se pudo completar el guardado. Vuelve a intentarlo.");
        }
        return "clubes/nuevo";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable("id") Long id, Model model) {
        Club club = clubService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe el club solicitado"));
        model.addAttribute("club", club);
        return "clubes/detalle";
    }
}
