package com.gestionclubes.repository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import com.gestionclubes.models.Coordinador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoordinadorRepository extends JpaRepository<Coordinador, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Coordinador> findByUsuario_Id(Long usuarioId);
}
