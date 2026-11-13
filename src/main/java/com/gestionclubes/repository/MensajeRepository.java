package com.gestionclubes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionclubes.models.Mensaje;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    @Query("""
        select m from Mensaje m
        join fetch m.remitente
        join fetch m.destinatario
        where lower(m.destinatario.email) = lower(:email)
          and m.leido = false
        order by m.enviadoEn desc
        """)
    List<Mensaje> noLeidos(@Param("email") String email);

    @Query("""
        select distinct m from Mensaje m
        join fetch m.remitente
        join fetch m.destinatario
        where (m.remitente.id = :primero and m.destinatario.id = :segundo)
           or (m.remitente.id = :segundo and m.destinatario.id = :primero)
        order by m.enviadoEn asc
        """)
    List<Mensaje> conversacion(@Param("primero") Long primero, @Param("segundo") Long segundo);
}
