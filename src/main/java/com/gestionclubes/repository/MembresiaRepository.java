package com.gestionclubes.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.gestionclubes.models.Membresia;

public interface MembresiaRepository extends JpaRepository<Membresia, Long> {
    Optional<Membresia> findByEstudiante_CarneAndClub_Id(String carne, Long clubId);
}
