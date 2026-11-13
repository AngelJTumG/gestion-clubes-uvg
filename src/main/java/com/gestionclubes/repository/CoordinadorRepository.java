package com.gestionclubes.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import com.gestionclubes.models.Coordinador;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Búsquedas de coordinadores asociados a una cuenta de usuario.
 */
public interface CoordinadorRepository extends JpaRepository<Coordinador, Long> {
    /** Busca y bloquea el coordinador asociado al usuario durante la transacción. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Coordinador> findByUsuario_Id(Long usuarioId);
}
