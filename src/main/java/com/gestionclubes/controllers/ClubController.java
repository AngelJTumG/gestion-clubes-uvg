package com.gestionclubes.controllers;

import java.security.Principal;

import com.gestionclubes.dtos.RegistroClubDto;
import com.gestionclubes.models.Club;
import com.gestionclubes.service.ClubService;

import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.security.core.Authentication;

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
                    "Club creado correctamente");
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
    public String detalle(@PathVariable("id") Long id, Model model, Authentication authentication) {
        Club club = clubService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe el club solicitado"));
        
        boolean puedeCrearClub = authentication.getAuthorities()
            .stream()
            .anyMatch(rol ->
            rol.getAuthority().equals("ROLE_ADMIN")
        );

        if (!club.isStatus() && !puedeCrearClub) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No existe el club solicitado"
            );
        }        

        model.addAttribute("club", club);
        model.addAttribute("puedeCrearClub", puedeCrearClub);

        return "clubes/detalle";
    }

    @GetMapping("/{id}/editar")
    public String formularioEdicion(@PathVariable("id") Long id, Model model) {
        try {
            model.addAttribute("club", clubService.obtenerFormularioEdicion(id));
            model.addAttribute("clubId", id);
            return "clubes/editar";
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("club") RegistroClubDto dto,
                             BindingResult result, Model model,
                             RedirectAttributes redirect) {
        model.addAttribute("clubId", id);
        if (result.hasErrors()) return "clubes/editar";
        try {
            Club club = clubService.actualizar(id, dto);
            redirect.addFlashAttribute("exito", "Información del club actualizada correctamente");
            return "redirect:/admin/clubes/" + club.getId();
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            result.reject("club.error", ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            log.warn("Una restricción de integridad impidió actualizar el club {}", id, ex);
            result.reject("club.integridad",
                    "No se pudieron guardar los cambios. Comprueba el nombre y vuelve a intentar.");
        } catch (TransientDataAccessException ex) {
            log.warn("Conflicto temporal al actualizar el club {}", id, ex);
            result.reject("club.temporal", "No se pudieron guardar los cambios. Vuelve a intentarlo.");
        }
        return "clubes/editar";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(
        @PathVariable("id") Long id,
        RedirectAttributes redirect) {

    try {
        clubService.desactivar(id);
    } catch (EntityNotFoundException ex) {
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            ex
        );
    }
        redirect.addFlashAttribute(
            "exito",
            "Club desactivado correctamente"
        );

        return "redirect:/admin/clubes/" + id;
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable("id") Long id,
                          RedirectAttributes redirect) {
        try {
            clubService.activar(id);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }

        redirect.addFlashAttribute("exito", "Club activado correctamente");
        return "redirect:/admin/clubes/" + id;
    }

}
