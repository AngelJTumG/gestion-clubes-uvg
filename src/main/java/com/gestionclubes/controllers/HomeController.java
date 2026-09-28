package com.gestionclubes.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    String inicio() { return "index"; }

    @GetMapping("/panel")
    String panel() { return "panel"; }
}

