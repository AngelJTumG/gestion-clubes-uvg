package com.gestionclubes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionclubes.models.Fotografia;

public interface FotografiaRepository
        extends JpaRepository<Fotografia, Long> {

    List<Fotografia> findByClub_IdOrderByIdAsc(Long idClub);
}

