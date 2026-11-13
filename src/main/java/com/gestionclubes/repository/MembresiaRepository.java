package com.gestionclubes.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.gestionclubes.models.Membresia;
import com.gestionclubes.models.Usuario;

/**
 * Consultas de membresías por club o estudiante, y de usuarios integrantes.
 */
public interface MembresiaRepository extends JpaRepository<Membresia, Long> {
    Optional<Membresia> findByEstudiante_CarneAndClub_Id(String carne, Long clubId);
    @EntityGraph(attributePaths = {"estudiante", "estudiante.usuario"})
    List<Membresia> findByClub_IdOrderByFechaIngresoAsc(Long clubId);
    @EntityGraph(attributePaths = {"club"})
    List<Membresia> findByEstudiante_Usuario_EmailIgnoreCaseOrderByFechaIngresoAsc(String email);

    /** Lista usuarios que pertenecen a clubes, excluyendo al usuario indicado. */
    @Query("""
        select distinct m.estudiante.usuario from Membresia m
        where lower(m.estudiante.usuario.email) <> lower(:email)
        order by m.estudiante.usuario.nombre
        """)
    List<Usuario> findUsuariosDeClubesExcepto(@Param("email") String email);

    boolean existsByEstudiante_Usuario_EmailIgnoreCase(String email);
}
