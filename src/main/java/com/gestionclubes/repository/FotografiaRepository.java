package com.gestionclubes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import com.gestionclubes.models.Fotografia;

/**
 * Consultas de fotografías publicadas en los clubes.
 */
public interface FotografiaRepository
        extends JpaRepository<Fotografia, Long> {

    List<Fotografia> findByClub_IdOrderByIdAsc(Long idClub);

    @EntityGraph(attributePaths = {"club", "club.coordinador", "club.coordinador.usuario"})
    java.util.Optional<Fotografia> findOneById(Long id);
}

