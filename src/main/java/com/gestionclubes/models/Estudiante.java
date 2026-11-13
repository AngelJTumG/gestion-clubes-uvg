package com.gestionclubes.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

/**
 * Datos estudiantiles asociados a una cuenta; el carné identifica al estudiante.
 */
@Entity
@Table(name = "estudiantes")
public class Estudiante {
    @Id
    private String carne;

    @OneToOne(optional = false)
    private Usuario usuario;

    public String getCarne() {
        return carne;
    }

    public void setCarne(String carne) {
        this.carne = carne;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
