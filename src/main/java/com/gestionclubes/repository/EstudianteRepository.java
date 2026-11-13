package com.gestionclubes.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.gestionclubes.models.Estudiante;

/**
 * Consultas de estudiantes por carné o por la cuenta de usuario asociada.
 */
public interface EstudianteRepository extends JpaRepository<Estudiante, String> {
    Optional<Estudiante> findByUsuario_Id(Long usuarioId);
    boolean existsByUsuario_EmailIgnoreCase(String email);
}
