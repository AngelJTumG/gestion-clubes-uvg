package com.gestionclubes.controllers;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionclubes.service.MensajeService;
import com.gestionclubes.service.AmistadService;
import com.gestionclubes.service.UsuarioService;

@Controller
public class ChatController {
    private final MensajeService mensajeService;
    private final UsuarioService usuarioService;
    private final AmistadService amistadService;

    public ChatController(MensajeService mensajeService, UsuarioService usuarioService,
                          AmistadService amistadService) {
        this.mensajeService = mensajeService;
        this.usuarioService = usuarioService;
        this.amistadService = amistadService;
    }

    @GetMapping("/chats")
    public String chats(Principal principal, Model model) {
        model.addAttribute("amistades", amistadService.amistades(principal.getName()));
        return "chats";
    }

    @GetMapping("/chat/{usuarioId}")
    public String chat(@PathVariable Long usuarioId, Principal principal, Model model,
                       RedirectAttributes redirect) {
        try {
            model.addAttribute("amigo", usuarioService.buscarPorId(usuarioId));
            model.addAttribute("mensajes", mensajeService.conversacion(principal.getName(), usuarioId));
            return "chat";
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/personas";
        }
    }

    @PostMapping("/chat/{usuarioId}/mensajes")
    public String enviar(@PathVariable Long usuarioId, @RequestParam String contenido,
                         Principal principal, RedirectAttributes redirect) {
        try {
            mensajeService.enviar(principal.getName(), usuarioId, contenido);
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/chat/" + usuarioId;
    }

    @GetMapping(value = "/api/chat/{usuarioId}/mensajes", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<MensajeView> mensajes(@PathVariable Long usuarioId, Principal principal) {
        return mensajeService.conversacion(principal.getName(), usuarioId).stream()
                .map(mensaje -> new MensajeView(
                        mensaje.getId(),
                        mensaje.getContenido(),
                        mensaje.getEnviadoEn().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")),
                        mensaje.getRemitente().getEmail().equalsIgnoreCase(principal.getName())))
                .toList();
    }

    @PostMapping(value = "/api/chat/{usuarioId}/mensajes",
                 consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public MensajeView enviarApi(@PathVariable Long usuarioId, @RequestParam String contenido,
                                 Principal principal) {
        mensajeService.enviar(principal.getName(), usuarioId, contenido);
        List<MensajeView> mensajes = mensajes(usuarioId, principal);
        return mensajes.get(mensajes.size() - 1);
    }

    @GetMapping(value = "/api/notificaciones/no-leidas", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public NotificationView notificacionesNoLeidas(Principal principal) {
        return new NotificationView(
                mensajeService.noLeidos(principal.getName()).size(),
                amistadService.notificacionesRecibidas(principal.getName()).size());
    }

    public record MensajeView(Long id, String contenido, String enviadoEn, boolean mio) {}

    public record NotificationView(int mensajes, int solicitudesAmistad) {}
}
