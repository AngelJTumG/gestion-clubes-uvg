package com.gestionclubes.controllers;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionclubes.dtos.RegistroClubDto;
import com.gestionclubes.models.Club;
import com.gestionclubes.models.EstadoSolicitud;
import com.gestionclubes.service.ClubService;
import com.gestionclubes.service.ActividadService;
import com.gestionclubes.service.EstudianteService;
import com.gestionclubes.service.MembresiaService;
import com.gestionclubes.service.SolicitudIngresoService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

/**
 * Atiende las operaciones de creación, consulta y administración de clubes.
 */
@Controller
@RequestMapping("/admin/clubes")
public class ClubController {
    private static final Logger log = LoggerFactory.getLogger(ClubController.class);
    private final ClubService clubService;
    private final SolicitudIngresoService solicitudService;
    private final EstudianteService estudianteService;
    private final ActividadService actividadService;
    private final MembresiaService membresiaService;

    public ClubController(ClubService clubService, SolicitudIngresoService solicitudService,
                          EstudianteService estudianteService, ActividadService actividadService,
                          MembresiaService membresiaService) {
        this.clubService = clubService;
        this.solicitudService = solicitudService;
        this.estudianteService = estudianteService;
        this.actividadService = actividadService;
        this.membresiaService = membresiaService;
    }

    /** Prepara el formulario de creación con el usuario actual como coordinador. */
    @GetMapping("/nuevo")
    public String formulario(Model model, Principal principal) {
        RegistroClubDto dto = new RegistroClubDto();
        dto.setCoordinadorEmail(principal.getName());
        model.addAttribute("club", dto);
        return "clubes/nuevo";
    }

    /** Valida y guarda un club, o devuelve el formulario con los errores. */
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

    /** Muestra el club y las acciones disponibles según el rol del usuario. */
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
        boolean puedeGestionarClub = puedeCrearClub || authentication.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN_CLUB")
                        && club.getCoordinador().getUsuario().getEmail().equalsIgnoreCase(authentication.getName()));

        if (!club.isStatus() && !puedeCrearClub) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No existe el club solicitado"
            );
        }        

        boolean puedeSolicitar = authentication.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_USUARIO"));
        EstadoSolicitud estadoSolicitud = puedeSolicitar
                ? solicitudService.estadoPara(id, authentication.getName()).orElse(null)
                : null;

        model.addAttribute("club", club);
        model.addAttribute("fotografias", clubService.consultarFotografias(id));
        model.addAttribute("actividades", actividadService.listarPorClub(id));
        model.addAttribute("puedeCrearClub", puedeCrearClub);
        model.addAttribute("puedeGestionarClub", puedeGestionarClub);
        model.addAttribute("puedeSolicitar", puedeSolicitar && club.isStatus());
        model.addAttribute("estadoSolicitud", estadoSolicitud);
        model.addAttribute("requiereCarne", puedeSolicitar && !estudianteService.tieneCarne(authentication.getName()));
        model.addAttribute("esMiembro", membresiaService.esMiembro(authentication.getName(), id));

        return "clubes/detalle";
    }

    /** Comprueba permisos y prepara el formulario con los datos actuales del club. */
    @GetMapping("/{id}/editar")
    public String formularioEdicion(@PathVariable("id") Long id, Model model, Authentication authentication) {
        try {
            verificarGestion(id, authentication);
            model.addAttribute("club", clubService.obtenerFormularioEdicion(id));
            model.addAttribute("clubId", id);
            return "clubes/editar";
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    /** Valida y guarda cambios del club después de comprobar los permisos. */
    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("club") RegistroClubDto dto,
                             BindingResult result, Model model,
                             Authentication authentication,
                             RedirectAttributes redirect) {
        verificarGestion(id, authentication);
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

    /** Desactiva el club y regresa a su página de detalle. */
    @PostMapping("/{id}/desactivar")
    public String desactivar(
        @PathVariable("id") Long id,
        Authentication authentication,
        RedirectAttributes redirect) {

    try {
        verificarGestion(id, authentication);
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

    /** Activa el club y regresa a su página de detalle. */
    @PostMapping("/{id}/activar")
    public String activar(@PathVariable("id") Long id,
                          Authentication authentication,
                          RedirectAttributes redirect) {
        try {
            verificarGestion(id, authentication);
            clubService.activar(id);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }

        redirect.addFlashAttribute("exito", "Club activado correctamente");
        return "redirect:/admin/clubes/" + id;
    }

    /** Permite gestionar el club solo a su coordinador o a un administrador. */
    private void verificarGestion(Long id, Authentication authentication) {
        Club club = clubService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe el club solicitado"));
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN"));
        boolean coordinador = authentication.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN_CLUB"));
        if (!admin && (!coordinador
                || !club.getCoordinador().getUsuario().getEmail().equalsIgnoreCase(authentication.getName()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para gestionar este club");
        }
    }

}
