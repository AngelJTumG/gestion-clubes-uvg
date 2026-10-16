package com.gestionclubes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionclubes.models.EstadoSolicitud;
import com.gestionclubes.models.SolicitudIngreso;

public interface SolicitudIngresoRepository extends JpaRepository<SolicitudIngreso, Long> {
    Optional<SolicitudIngreso> findByUsuario_EmailIgnoreCaseAndClub_Id(String email, Long clubId);

    @EntityGraph(attributePaths = {"usuario", "club"})
    List<SolicitudIngreso> findByClub_Coordinador_Usuario_EmailIgnoreCaseAndEstadoOrderByCreadaEnAsc(
            String email, EstadoSolicitud estado);

    @EntityGraph(attributePaths = {"usuario", "club"})
    List<SolicitudIngreso> findByEstadoOrderByCreadaEnAsc(EstadoSolicitud estado);
}
