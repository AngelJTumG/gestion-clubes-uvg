package com.gestionclubes.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity 
@Table(name = "coordinadores")
public class Coordinador {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "coordinador")
    private List<Club> clubesAdministrados = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<Club> getClubesAdministrados() {
        return clubesAdministrados;
    }

    public void setClubesAdministrados(List<Club> clubesAdministrados) {
        this.clubesAdministrados = clubesAdministrados;
    }
}
