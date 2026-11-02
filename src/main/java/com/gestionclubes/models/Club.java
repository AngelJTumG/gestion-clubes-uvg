package com.gestionclubes.models;

import java.util.List;
import java.util.Collections;
import java.time.LocalDate;
import java.util.Objects;
import java.util.ArrayList;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;

@Entity
@Table(name = "clubs", uniqueConstraints = @UniqueConstraint(name = "uk_club_nombre", columnNames = "nombre"))
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Column(nullable = false, length = 100)
    private String horario;

    @Column (nullable = false, length = 100)
    private String ubicacion;

    @Column(nullable = false, length = 100)
    private String requisitos;

    @Column (nullable = false, length = 255)
    private String datosContacto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "coordinador_id", nullable = false)
    private Coordinador coordinador;

    @OneToMany (mappedBy = "club")
    private List<Fotografia> fotografias = new ArrayList<>();

    @OneToMany (mappedBy = "club")
    private List<Actividad> actividades = new ArrayList<>();

    @OneToMany(mappedBy = "club")
    private List<Membresia> membresias = new ArrayList<>();

    @Column(nullable = false)
    private boolean status = true;

    public List<Membresia> getMembresias() {
        return Collections.unmodifiableList(membresias);
    }

    public int getNumeroIntegrantes() {
        return membresias.size();
    }

    public Membresia agregarIntegrante(Estudiante estudiante, LocalDate fechaIngreso) {
        Objects.requireNonNull(estudiante, "El integrante requiere un estudiante");
        Objects.requireNonNull(fechaIngreso, "La membresía requiere fecha de ingreso");
        if (estudiante.getCarne() == null || estudiante.getCarne().isBlank()) {
            throw new IllegalArgumentException("El estudiante debe tener un carné registrado");
        }
        for (Membresia membresia : membresias) {
            if (membresia.getEstudiante().getCarne().equals(estudiante.getCarne())) {
                return membresia;
            }
        }
        Membresia membresia = new Membresia(estudiante, this, fechaIngreso);
        membresias.add(membresia);
        return membresia;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getHorario() {
        return horario;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public String getRequisitos() {
        return requisitos;
    }

    public String getDatosContacto() {
        return datosContacto;
    }

    public Coordinador getCoordinador() {
        return coordinador;
    }

    public List<Fotografia> getFotografias() {
        return fotografias;
    }

    public List<Actividad> getActividades() {
        return actividades;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos;
    }

    public void setDatosContacto(String datosContacto) {
        this.datosContacto = datosContacto;
    }

    public void setCoordinador(Coordinador coordinador) {
        java.util.Objects.requireNonNull(coordinador, "El club requiere un coordinador");
        if (this.coordinador == coordinador) return;
        Coordinador anterior = this.coordinador;
        this.coordinador = coordinador;
        if (anterior != null) anterior.getClubesAdministrados().remove(this);
        if (!coordinador.getClubesAdministrados().contains(this)) {
            coordinador.getClubesAdministrados().add(this);
        }
    }

    public void setFotografias(List<Fotografia> fotografias) {
        this.fotografias = fotografias;
    }

    public void setActividades(List<Actividad> actividades) {
        this.actividades = actividades;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

}
