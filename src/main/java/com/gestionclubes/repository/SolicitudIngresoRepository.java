package com.gestionclubes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.gestionclubes.models.EstadoSolicitud;
import com.gestionclubes.models.SolicitudIngreso;

/**
 * Consultas de solicitudes de ingreso por usuario, club y estado.
 */
public interface SolicitudIngresoRepository extends JpaRepository<SolicitudIngreso, Long> {
    /** Obtiene y bloquea una solicitud para evitar cambios simultáneos al gestionarla. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SolicitudIngreso s where s.id = :id")
    Optional<SolicitudIngreso> buscarParaGestionar(@Param("id") Long id);

    List<SolicitudIngreso> findByUsuario_IdAndEstado(Long usuarioId, EstadoSolicitud estado);

    Optional<SolicitudIngreso> findByUsuario_EmailIgnoreCaseAndClub_Id(String email, Long clubId);

    @EntityGraph(attributePaths = {"club"})
    List<SolicitudIngreso> findByUsuario_EmailIgnoreCaseOrderByCreadaEnDesc(String email);

    @EntityGraph(attributePaths = {"usuario", "club"})
    List<SolicitudIngreso> findByClub_Coordinador_Usuario_EmailIgnoreCaseAndEstadoOrderByCreadaEnAsc(
            String email, EstadoSolicitud estado);

    @EntityGraph(attributePaths = {"usuario", "club"})
    List<SolicitudIngreso> findByEstadoOrderByCreadaEnAsc(EstadoSolicitud estado);
}
