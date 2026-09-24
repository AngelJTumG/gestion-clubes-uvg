package com.gestionclubes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionclubes.models.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}

