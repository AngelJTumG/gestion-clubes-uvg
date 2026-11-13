package com.gestionclubes.config;

import com.gestionclubes.models.Rol;
import com.gestionclubes.models.Usuario;
import com.gestionclubes.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email:}") private String email;
    @Value("${app.admin.password:}") private String password;

    public AdminInitializer(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.isBlank() || repository.existsByEmailIgnoreCase(email)) return;
        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setEmail(email.trim().toLowerCase());
        admin.setPassword(encoder.encode(password));
        admin.setRol(Rol.ADMIN);
        repository.save(admin);
    }
}

