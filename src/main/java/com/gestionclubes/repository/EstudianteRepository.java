package com.gestionclubes.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.gestionclubes.models.Estudiante;

public interface EstudianteRepository extends JpaRepository<Estudiante, String> {
    Optional<Estudiante> findByUsuario_Id(Long usuarioId);
    boolean existsByUsuario_EmailIgnoreCase(String email);
}
