package com.gestionclubes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestionclubes.models.Estudiante;
import com.gestionclubes.models.Usuario;
import com.gestionclubes.repository.EstudianteRepository;

@Service
public class EstudianteService {
    private final EstudianteRepository estudianteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    @Transactional(readOnly = true)
    public boolean tieneCarne(String email) {
        return estudianteRepository.existsByUsuario_EmailIgnoreCase(email);
    }

    @Transactional
    public Estudiante registrar(Usuario usuario, String carne) {
        String normalizado = carne == null ? "" : carne.trim();
        if (!normalizado.matches("[0-9]{1,30}")) {
            throw new IllegalArgumentException("Ingresa un carné de 1 a 30 dígitos");
        }
        Estudiante existente = estudianteRepository.findByUsuario_Id(usuario.getId()).orElse(null);
        if (existente != null) {
            if (!existente.getCarne().equals(normalizado)) {
                throw new IllegalArgumentException("Tu cuenta ya tiene un carné registrado");
            }
            return existente;
        }
        if (estudianteRepository.existsById(normalizado)) {
            throw new IllegalArgumentException("Ese carné ya está asociado a otra cuenta");
        }
        Estudiante estudiante = new Estudiante();
        estudiante.setCarne(normalizado);
        estudiante.setUsuario(usuario);
        return estudianteRepository.saveAndFlush(estudiante);
    }
}
