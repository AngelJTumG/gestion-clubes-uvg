package com.gestionclubes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import com.gestionclubes.models.Actividad;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    @EntityGraph(attributePaths = {"club"})
    List<Actividad> findAllByOrderByFechaAscHoraInicioAsc();
    @EntityGraph(attributePaths = {"club"})
    List<Actividad> findByClub_IdOrderByFechaAscHoraInicioAsc(Long clubId);
    @EntityGraph(attributePaths = {"club"})
    java.util.Optional<Actividad> findOneById(Long id);
}
