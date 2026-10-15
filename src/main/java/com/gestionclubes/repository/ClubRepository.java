package com.gestionclubes.repository;

import java.util.Optional;
import com.gestionclubes.models.Club;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long> {
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
    
    @EntityGraph(attributePaths = {"coordinador", "coordinador.usuario"})
    Optional<Club> findOneById(Long id);

    List<Club> findByStatusTrue();
}
