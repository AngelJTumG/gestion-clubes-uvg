
package com.gestionclubes.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.MediaType;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.gestionclubes.service.ClubService;
import com.gestionclubes.service.AmistadService;
import com.gestionclubes.service.SolicitudIngresoService;
import com.gestionclubes.service.UsuarioService;

/**
 * Muestra la portada pública y el panel principal con clubes y notificaciones.
 */
@Controller
public class HomeController {
    @GetMapping("/configuracion")
    public String configuracion() {
        return "configuracion";
    }

    private final ClubService clubService;
    private final SolicitudIngresoService solicitudService;
    private final AmistadService amistadService;
    private final UsuarioService usuarioService;

    public HomeController(ClubService clubService, SolicitudIngresoService solicitudService,
                          AmistadService amistadService, UsuarioService usuarioService) {
        this.clubService = clubService;
        this.solicitudService = solicitudService;
        this.amistadService = amistadService;
        this.usuarioService = usuarioService;
    }

    /** Muestra la página pública de inicio. */
    @GetMapping("/")
    String inicio() {
        return "index";
    }

    /** Carga clubes filtrados y datos del panel según el usuario y sus permisos. */
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
        boolean esCoordinador = authentication.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN_CLUB"));

        model.addAttribute("puedeCrearClub", puedeCrearClub);
        model.addAttribute("puedeGestionarSolicitudes", puedeGestionarSolicitudes);
        model.addAttribute("puedeAprobarTodas", esAdministradorGlobal);
        model.addAttribute("esCoordinador", esCoordinador);
        if (esCoordinador) {
            model.addAttribute("clubesCoordinados", clubService.listarPorCoordinador(authentication.getName())
                    .stream().map(club -> club.getId()).collect(Collectors.toSet()));
        }
        if (puedeGestionarSolicitudes) {
            model.addAttribute("solicitudes",
                solicitudService.pendientesPara(authentication.getName(), esAdministradorGlobal));
        }

        model.addAttribute("busqueda", busqueda);
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("notificaciones", solicitudService.notificacionesDe(authentication.getName()));
        model.addAttribute("notificacionesAmistad", amistadService.notificaciones(authentication.getName()));
        model.addAttribute("notificacionesAmistadRecibidas",
                amistadService.notificacionesRecibidas(authentication.getName()));
        model.addAttribute("usuarioActual", usuarioService.buscarPorEmail(authentication.getName()));
        model.addAttribute("categorias", clubService.listarCategorias(puedeCrearClub));
        model.addAttribute("clubes", clubService.filtrarClubes(
                puedeCrearClub, busqueda, categoria, estado));

        return "panel";
    }

    @GetMapping(value = "/api/admin/solicitudes/pendientes",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    SolicitudesPendientesView solicitudesPendientes(Authentication authentication) {
        boolean administradorGlobal = authentication.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN"));
        boolean puedeGestionar = administradorGlobal || authentication.getAuthorities().stream()
                .anyMatch(rol -> rol.getAuthority().equals("ROLE_ADMIN_CLUB"));
        if (!puedeGestionar) {
            return new SolicitudesPendientesView(List.of(), false);
        }

        List<SolicitudPendienteView> solicitudes = solicitudService
                .pendientesPara(authentication.getName(), administradorGlobal).stream()
                .map(solicitud -> new SolicitudPendienteView(
                        solicitud.getId(),
                        solicitud.getUsuario().getNombre(),
                        solicitud.getClub().getNombre(),
                        solicitud.getCreadaEn().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))))
                .toList();
        return new SolicitudesPendientesView(solicitudes, administradorGlobal);
    }

    record SolicitudPendienteView(Long id, String usuario, String club, String creadaEn) {}

    record SolicitudesPendientesView(List<SolicitudPendienteView> solicitudes,
                                     boolean puedeAprobarTodas) {}
}
