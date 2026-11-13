package com.gestionclubes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionclubes.models.EstadoSolicitud;
import com.gestionclubes.models.SolicitudAmistad;

public interface SolicitudAmistadRepository extends JpaRepository<SolicitudAmistad, Long> {
    @EntityGraph(attributePaths = {"solicitante", "destinatario"})
    List<SolicitudAmistad> findByDestinatario_EmailIgnoreCaseOrderByCreadaEnDesc(String email);

    @EntityGraph(attributePaths = {"solicitante", "destinatario"})
    List<SolicitudAmistad> findBySolicitante_EmailIgnoreCaseOrderByCreadaEnDesc(String email);

    Optional<SolicitudAmistad> findBySolicitante_IdAndDestinatario_Id(Long solicitanteId, Long destinatarioId);

    @Query("""
        select s from SolicitudAmistad s
        where (s.solicitante.id = :primero and s.destinatario.id = :segundo)
           or (s.solicitante.id = :segundo and s.destinatario.id = :primero)
        """)
    Optional<SolicitudAmistad> buscarEntre(@Param("primero") Long primero, @Param("segundo") Long segundo);

    @EntityGraph(attributePaths = {"solicitante", "destinatario"})
    List<SolicitudAmistad> findByDestinatario_EmailIgnoreCaseAndEstadoOrderByCreadaEnDesc(
            String email, EstadoSolicitud estado);

    @EntityGraph(attributePaths = {"solicitante", "destinatario"})
    List<SolicitudAmistad> findBySolicitante_EmailIgnoreCaseAndEstadoOrderByCreadaEnDesc(
            String email, EstadoSolicitud estado);
}
