package com.gestionclubes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionclubes.models.Usuario;

import jakarta.persistence.LockModeType;

import java.util.Optional;

/**
 * Consultas de cuentas de usuario, principalmente por correo electrónico.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    /** Busca por correo y bloquea la cuenta durante la transacción actual. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where lower(u.email) = lower(:email)")
    Optional<Usuario> buscarPorEmailParaActualizar(@Param("email") String email);
}

