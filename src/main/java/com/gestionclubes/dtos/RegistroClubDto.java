package com.gestionclubes.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistroClubDto {
    @NotBlank(message = "Nombre: este campo es obligatorio")
    @Size(max = 100, message = "Nombre: máximo 100 caracteres")
    private String nombre;

    @NotBlank(message = "Descripción: este campo es obligatorio")
    @Size(max = 255, message = "Descripción: máximo 255 caracteres")
    private String descripcion;

    @NotBlank(message = "Categoría: este campo es obligatorio")
    @Size(max = 100, message = "Categoría: máximo 100 caracteres")
    private String categoria;

    @NotBlank(message = "Horario: este campo es obligatorio")
    @Size(max = 100, message = "Horario: máximo 100 caracteres")
    private String horario;

    @NotBlank(message = "Ubicación: este campo es obligatorio")
    @Size(max = 100, message = "Ubicación: máximo 100 caracteres")
    private String ubicacion;

    @NotBlank(message = "Requisitos: este campo es obligatorio")
    @Size(max = 100, message = "Requisitos: máximo 100 caracteres")
    private String requisitos;

    @NotBlank(message = "Datos de contacto: este campo es obligatorio")
    @Size(max = 255, message = "Datos de contacto: máximo 255 caracteres")
    private String datosContacto;

    @NotBlank(message = "Correo del coordinador: este campo es obligatorio")
    @Email(message = "Ingresa un correo electrónico válido")
    @Size(max = 150, message = "Correo del coordinador: máximo 150 caracteres")
    private String coordinadorEmail;

    public String getNombre() { 
        return nombre; 
    }

    public void setNombre(String nombre) {
        this.nombre = nombre == null ? null : nombre.trim();
    }

    public String getDescripcion() { 
        return descripcion; 
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion == null ? null : descripcion.trim();
    }

    public String getCategoria() { 
        return categoria; 
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria == null ? null : categoria.trim();
    }

    public String getHorario() { 
        return horario; 
    }

    public void setHorario(String horario) {
        this.horario = horario == null ? null : horario.trim();
    }

    public String getUbicacion() { 
        return ubicacion; 
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion == null ? null : ubicacion.trim();
    }

    public String getRequisitos() { 
        return requisitos; 
    }

    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos == null ? null : requisitos.trim();
    }

    public String getDatosContacto() { 
        return datosContacto; 
    }
    
    public void setDatosContacto(String datosContacto) {
        this.datosContacto = datosContacto == null ? null : datosContacto.trim();
    }

    public String getCoordinadorEmail() { 
        return coordinadorEmail; 
    }

    public void setCoordinadorEmail(String coordinadorEmail) {
        this.coordinadorEmail = coordinadorEmail == null ? null : coordinadorEmail.trim();
    }

}
