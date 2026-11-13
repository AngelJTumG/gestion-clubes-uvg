package com.gestionclubes.controllers;

import java.security.Principal;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.YearMonth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionclubes.models.Actividad;
import com.gestionclubes.models.Fotografia;
import com.gestionclubes.models.Rol;
import com.gestionclubes.service.AmistadService;
import com.gestionclubes.service.ActividadService;
import com.gestionclubes.service.CalendarioService;
import com.gestionclubes.service.ClubService;
import com.gestionclubes.service.FotografiaService;
import com.gestionclubes.service.MembresiaService;
import com.gestionclubes.service.MensajeService;
import com.gestionclubes.service.SolicitudIngresoService;
import com.gestionclubes.service.UsuarioService;

/**
 * Atiende las páginas de actividades, membresías, perfil, calendario y notificaciones,
 * además de las operaciones de gestión de clubes.
 */
@Controller
public class GestionClubController {
    private final ActividadService actividadService;
    private final ClubService clubService;
    private final FotografiaService fotografiaService;
    private final CalendarioService calendarioService;
    private final MembresiaService membresiaService;
    private final SolicitudIngresoService solicitudService;
    private final UsuarioService usuarioService;
    private final AmistadService amistadService;
    private final MensajeService mensajeService;

    public GestionClubController(ActividadService actividadService, ClubService clubService,
                                 FotografiaService fotografiaService,
    CalendarioService calendarioService,
    MembresiaService membresiaService,
    SolicitudIngresoService solicitudService,
    UsuarioService usuarioService,
    AmistadService amistadService,
    MensajeService mensajeService) {
        this.actividadService = actividadService;
        this.clubService = clubService;
        this.fotografiaService = fotografiaService;
        this.calendarioService = calendarioService;
        this.membresiaService = membresiaService;
        this.solicitudService = solicitudService;
        this.usuarioService = usuarioService;
        this.amistadService = amistadService;
        this.mensajeService = mensajeService;
    }

    /** Muestra las actividades publicadas en todos los clubes. */
    @GetMapping("/actividades")
    public String actividades(Model model) {
        model.addAttribute("actividades", actividadService.listarTodas());
        return "actividades";
    }

    /** Verifica el acceso y prepara el formulario para una actividad nueva. */
    @GetMapping("/club-admin/clubes/{clubId}/actividades/nueva")
    public String nuevaActividad(@PathVariable Long clubId, Model model, Principal principal) {
        verificarClub(clubId, principal);
        clubService.buscarPorId(clubId).orElseThrow(() -> noEncontrado("No existe el club solicitado"));
        model.addAttribute("clubId", clubId);
        model.addAttribute("actividad", new Actividad());
        return "clubes/actividad-form";
    }

    /** Crea una actividad para el club y regresa a su lista de actividades. */
    @PostMapping("/club-admin/clubes/{clubId}/actividades")
    public String crearActividad(@PathVariable Long clubId, @ModelAttribute Actividad actividad,
                                 Principal principal, RedirectAttributes redirect) {
        try {
            actividadService.crear(clubId, actividad, principal.getName(), esAdministrador(principal));
            redirect.addFlashAttribute("exito", "Actividad publicada correctamente");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/club-admin/clubes/" + clubId + "/actividades";
    }

    /** Muestra las actividades del club para que su responsable las gestione. */
    @GetMapping("/club-admin/clubes/{clubId}/actividades")
    public String actividadesClub(@PathVariable Long clubId, Model model, Principal principal) {
        verificarClub(clubId, principal);
        model.addAttribute("club", clubService.buscarPorId(clubId)
                .orElseThrow(() -> noEncontrado("No existe el club solicitado")));
        model.addAttribute("actividades", actividadService.listarPorClub(clubId));
        model.addAttribute("clubId", clubId);
        return "clubes/actividades-gestion";
    }

    /** Prepara el formulario de edición después de verificar el club de la actividad. */
    @GetMapping("/club-admin/actividades/{id}/editar")
    public String editarActividad(@PathVariable Long id, Model model, Principal principal) {
        Actividad actividad = actividadService.buscar(id);
        verificarClub(actividad.getClub().getId(), principal);
        model.addAttribute("actividad", actividad);
        model.addAttribute("clubId", actividad.getClub().getId());
        return "clubes/actividad-form";
    }

    /** Actualiza la actividad y regresa a la lista de su club. */
    @PostMapping("/club-admin/actividades/{id}")
    public String actualizarActividad(@PathVariable Long id, @ModelAttribute Actividad actividad,
                                      Principal principal, RedirectAttributes redirect) {
        Actividad existente = actividadService.buscar(id);
        try {
            actividadService.actualizar(id, actividad, principal.getName(), esAdministrador(principal));
            redirect.addFlashAttribute("exito", "Actividad actualizada correctamente");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/club-admin/clubes/" + existente.getClub().getId() + "/actividades";
    }

    /** Elimina una actividad y regresa a la lista de su club. */
    @PostMapping("/club-admin/actividades/{id}/eliminar")
    public String eliminarActividad(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        Actividad actividad = actividadService.buscar(id);
        Long clubId = actividad.getClub().getId();
        try {
            actividadService.eliminar(id, principal.getName(), esAdministrador(principal));
            redirect.addFlashAttribute("exito", "Actividad eliminada correctamente");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/club-admin/clubes/" + clubId + "/actividades";
    }

    /** Muestra la galería y el formulario para agregar fotografías al club. */
    @GetMapping("/club-admin/clubes/{clubId}/fotografias")
    public String fotografias(@PathVariable Long clubId, Model model, Principal principal) {
        verificarClub(clubId, principal);
        model.addAttribute("club", clubService.buscarPorId(clubId)
                .orElseThrow(() -> noEncontrado("No existe el club solicitado")));
        model.addAttribute("fotografias", fotografiaService.listarPorClub(clubId));
        model.addAttribute("fotografia", new Fotografia());
        return "clubes/fotografias";
    }

    /** Guarda una fotografía cargada y vuelve a la galería del club. */
    @PostMapping("/club-admin/clubes/{clubId}/fotografias")
    public String agregarFotografia(@PathVariable Long clubId, @ModelAttribute Fotografia fotografia,
                                    @org.springframework.web.bind.annotation.RequestParam(value = "archivo", required = false)
                                    MultipartFile archivo,
                                    Principal principal, RedirectAttributes redirect) {
        try {
            verificarClub(clubId, principal);
            fotografiaService.guardar(clubId, fotografia, archivo);
            redirect.addFlashAttribute("exito", "Fotografía agregada correctamente");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (IOException ex) {
            redirect.addFlashAttribute("error", "No se pudo guardar el archivo de imagen");
        }
        return "redirect:/club-admin/clubes/" + clubId + "/fotografias";
    }

    /** Elimina una fotografía del club y su archivo asociado cuando corresponde. */
    @PostMapping("/club-admin/fotografias/{id}/eliminar")
    public String eliminarFotografia(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        Fotografia fotografia = fotografiaService.buscar(id);
        Long clubId = fotografia.getClub().getId();
        try {
            verificarClub(clubId, principal);
            fotografiaService.eliminar(id);
            redirect.addFlashAttribute("exito", "Fotografía eliminada correctamente");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (IOException ex) {
            redirect.addFlashAttribute("error", "La fotografía se eliminó, pero no se pudo borrar el archivo físico");
        }
        return "redirect:/club-admin/clubes/" + clubId + "/fotografias";
    }

    /** Muestra los integrantes registrados para el club. */
    @GetMapping("/clubes/{clubId}/integrantes")
    public String integrantes(@PathVariable Long clubId, Model model) {
        model.addAttribute("club", clubService.buscarPorId(clubId)
                .orElseThrow(() -> noEncontrado("No existe el club solicitado")));
        model.addAttribute("integrantes", membresiaService.listarPorClub(clubId));
        return "clubes/integrantes";
    }

    @PostMapping("/club-admin/clubes/{clubId}/integrantes/{membresiaId}/retirar")
    public String retirarIntegrante(@PathVariable Long clubId, @PathVariable Long membresiaId,
                                    Principal principal, RedirectAttributes redirect) {
        verificarClub(clubId, principal);
        try {
            membresiaService.retirar(membresiaId, clubId);
            redirect.addFlashAttribute("exito", "Integrante retirado del club");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/clubes/" + clubId + "/integrantes";
    }

    /** Muestra las solicitudes de ingreso del usuario autenticado. */
    @GetMapping({"/solicitudes", "/mis-solicitudes"})
    public String misSolicitudes(Principal principal, Model model) {
        model.addAttribute("solicitudes", solicitudService.solicitudesDe(principal.getName()));
        return "solicitudes";
    }

    /** Muestra los clubes a los que pertenece el usuario y sus solicitudes. */
    @GetMapping("/mis-clubes")
    public String misClubes(Principal principal, Model model) {
        model.addAttribute("membresias", membresiaService.listarPorEstudiante(principal.getName()));
        model.addAttribute("solicitudes", solicitudService.solicitudesDe(principal.getName()));
        return "membresias";
    }

    /** Muestra las actividades organizadas en el calendario del mes actual. */
    @GetMapping("/calendario")
    public String calendario(@RequestParam(required = false) Integer year,
                             @RequestParam(required = false) Integer month,
                             Model model) {
        YearMonth mes = YearMonth.now();
        if (year != null && month != null) {
            try {
                mes = YearMonth.of(year, month);
            } catch (DateTimeException ex) {
                throw new IllegalArgumentException("El mes solicitado no es válido", ex);
            }
        }
        CalendarioService.CalendarioMensual calendario = calendarioService.mes(mes);
        model.addAttribute("dias", calendario.dias());
        model.addAttribute("mesActual", calendario.nombreMes());
        model.addAttribute("mesAnterior", mes.minusMonths(1));
        model.addAttribute("mesSiguiente", mes.plusMonths(1));
        model.addAttribute("mesActualSeleccionado", mes);
        return "calendario";
    }

    /** Muestra los datos del perfil del usuario autenticado. */
    @GetMapping("/perfil")
    public String perfil(Principal principal, Model model) {
        model.addAttribute("usuario", usuarioService.buscarPorEmail(principal.getName()));
        return "perfil";
    }

    /** Actualiza nombre, descripción y fotografía del perfil. */
    @PostMapping("/perfil")
    public String actualizarPerfil(Principal principal,
                                   @org.springframework.web.bind.annotation.RequestParam String nombre,
                                   @org.springframework.web.bind.annotation.RequestParam(required = false) String descripcion,
                                   @org.springframework.web.bind.annotation.RequestParam(value = "foto", required = false)
                                   MultipartFile foto,
                                   RedirectAttributes redirect) {
        try {
            usuarioService.actualizarPerfil(principal.getName(), nombre, descripcion, foto);
            redirect.addFlashAttribute("exito", "Perfil actualizado correctamente");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (IOException ex) {
            redirect.addFlashAttribute("error", "No se pudo guardar la foto de perfil");
        }
        return "redirect:/perfil";
    }

    /** Muestra las notificaciones sin marcarlas como leídas automáticamente. */
    @GetMapping("/notificaciones")
    public String notificaciones(Principal principal, Model model) {
        model.addAttribute("notificaciones", solicitudService.notificacionesDe(principal.getName()));
        model.addAttribute("notificacionesAmistad", amistadService.notificaciones(principal.getName()));
        model.addAttribute("solicitudesAmistadPendientes",
                amistadService.solicitudesPendientesRecibidas(principal.getName()));
        model.addAttribute("mensajesNoLeidos", mensajeService.noLeidos(principal.getName()));
        amistadService.marcarNotificacionesLeidas(principal.getName());
        amistadService.marcarSolicitudesRecibidasLeidas(principal.getName());
        return "notificaciones";
    }

    /** Permite la gestión solo al administrador global o al coordinador del club. */
    private void verificarClub(Long clubId, Principal principal) {
        var club = clubService.buscarPorId(clubId)
                .orElseThrow(() -> noEncontrado("No existe el club solicitado"));
        if (!esAdministrador(principal)
                && !club.getCoordinador().getUsuario().getEmail().equalsIgnoreCase(principal.getName())) {
            throw new IllegalArgumentException("No tienes permiso para gestionar este club");
        }
    }

    /** Indica si el usuario autenticado tiene el rol de administrador global. */
    private boolean esAdministrador(Principal principal) {
        return principal instanceof org.springframework.security.core.Authentication authentication
                && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Rol.ADMIN.name()));
    }

    /** Crea una respuesta HTTP 404 con el mensaje indicado. */
    private ResponseStatusException noEncontrado(String mensaje) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, mensaje);
    }
}
