package com.gestionclubes.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionclubes.models.Actividad;
import com.gestionclubes.models.Club;
import com.gestionclubes.repository.ActividadRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ActividadService {
    private final ActividadRepository actividadRepository;
    private final ClubService clubService;

    public ActividadService(ActividadRepository actividadRepository, ClubService clubService) {
        this.actividadRepository = actividadRepository;
        this.clubService = clubService;
    }

    @Transactional(readOnly = true)
    public List<Actividad> listarTodas() {
        return actividadRepository.findAllByOrderByFechaAscHoraInicioAsc();
    }

    @Transactional(readOnly = true)
    public List<Actividad> listarPorClub(Long clubId) {
        clubService.buscarPorId(clubId)
                .orElseThrow(() -> new EntityNotFoundException("No existe el club solicitado"));
        return actividadRepository.findByClub_IdOrderByFechaAscHoraInicioAsc(clubId);
    }

    @Transactional(readOnly = true)
    public Actividad buscar(Long id) {
        return actividadRepository.findOneById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe la actividad solicitada"));
    }

    @Transactional
    public Actividad crear(Long clubId, Actividad datos, String email, boolean administradorGlobal) {
        Club club = clubAutorizado(clubId, email, administradorGlobal);
        validar(datos);
        datos.setClub(club);
        return actividadRepository.save(datos);
    }

    @Transactional
    public Actividad actualizar(Long id, Actividad datos, String email, boolean administradorGlobal) {
        Actividad actividad = buscar(id);
        clubAutorizado(actividad.getClub().getId(), email, administradorGlobal);
        validar(datos);
        actividad.setNombre(datos.getNombre());
        actividad.setDescripcion(datos.getDescripcion());
        actividad.setFecha(datos.getFecha());
        actividad.setHoraInicio(datos.getHoraInicio());
        actividad.setUbicacion(datos.getUbicacion());
        return actividadRepository.save(actividad);
    }

    @Transactional
    public void eliminar(Long id, String email, boolean administradorGlobal) {
        Actividad actividad = buscar(id);
        clubAutorizado(actividad.getClub().getId(), email, administradorGlobal);
        actividadRepository.delete(actividad);
    }

    private Club clubAutorizado(Long clubId, String email, boolean administradorGlobal) {
        Club club = clubService.buscarPorId(clubId)
                .orElseThrow(() -> new EntityNotFoundException("No existe el club solicitado"));
        if (!administradorGlobal && !club.getCoordinador().getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("No tienes permiso para gestionar este club");
        }
        return club;
    }

    private void validar(Actividad actividad) {
        if (actividad.getNombre() == null || actividad.getNombre().isBlank()
                || actividad.getDescripcion() == null || actividad.getDescripcion().isBlank()
                || actividad.getFecha() == null || actividad.getHoraInicio() == null
                || actividad.getUbicacion() == null || actividad.getUbicacion().isBlank()) {
            throw new IllegalArgumentException("Completa todos los datos de la actividad");
        }
    }
}
