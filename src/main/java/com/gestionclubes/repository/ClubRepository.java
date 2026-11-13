package com.gestionclubes.repository;

import java.util.Optional;
import com.gestionclubes.models.Club;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long> {
    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
    
    @EntityGraph(attributePaths = {"coordinador", "coordinador.usuario", "membresias"})
    Optional<Club> findOneById(Long id);

    @Override
    @EntityGraph(attributePaths = {"membresias"})
    List<Club> findAll();

    @EntityGraph(attributePaths = {"membresias"})
    List<Club> findByStatusTrue();

    @EntityGraph(attributePaths = {"coordinador", "coordinador.usuario", "membresias"})
    List<Club> findByCoordinador_Usuario_EmailIgnoreCase(String email);
}
