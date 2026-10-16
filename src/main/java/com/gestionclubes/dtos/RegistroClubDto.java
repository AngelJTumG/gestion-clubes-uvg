package com.gestionclubes.dtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistroClubDto {
    @NotBlank(message = "Nombre: este campo es obligatorio")
    @Size(max = 100, message = "Nombre: máximo 100 caracteres")
    private String nombre;

    @NotBlank(message = "Descripción: este campo es obligatorio")
    @Size(max = 255, message = "Descripción: máximo 255 caracteres")
    private String descripcion;

    @NotBlank(message = "Categoría: este campo es obligatorio")
    @Pattern(regexp = "(?i)academico|deportivo|arte", message = "Selecciona una categoría válida")
    private String categoria;

    private String[] horarioDias1 = new String[]{""};
    private String[] horarioInicios1 = new String[]{""};
    private String[] horarioFines1 = new String[]{""};
    private String horario;

    private String ubicacionEdificio;
    private String ubicacionNumero;
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
        if (horarioDias1 == null) return "";
        StringBuilder horarioCompleto = new StringBuilder();
        for (int indice = 0; indice < horarioDias1.length; indice++) {
            String dia = horarioDias1[indice];
            String inicio = valorEn(horarioInicios1, indice);
            String fin = valorEn(horarioFines1, indice);
            if (!esHorarioValido(dia, inicio, fin)) continue;
            if (horarioCompleto.length() > 0) horarioCompleto.append("; ");
            horarioCompleto.append(dia).append(" ").append(inicio).append(" - ").append(fin);
        }
        return horarioCompleto.toString();
    }

    public void setHorario(String horario) {
        this.horario = horario == null ? null : horario.trim();
        horarioDias1 = new String[]{""};
        horarioInicios1 = new String[]{""};
        horarioFines1 = new String[]{""};
        if (this.horario == null || this.horario.isBlank()) return;

        String[] horarios = this.horario.split("\\s*;\\s*");
        horarioDias1 = new String[horarios.length];
        horarioInicios1 = new String[horarios.length];
        horarioFines1 = new String[horarios.length];
        for (int indice = 0; indice < horarios.length; indice++) {
            String[] partes = horarios[indice].split("\\s+", 2);
            if (partes.length == 2) {
                horarioDias1[indice] = partes[0];
                String[] horas = partes[1].split("\\s*-\\s*", 2);
                if (horas.length == 2) {
                    horarioInicios1[indice] = horas[0];
                    horarioFines1[indice] = horas[1];
                }
            }
        }
    }

    public String getUbicacion() { 
        if (ubicacionEdificio == null || ubicacionNumero == null) return ubicacion;
        return ubicacionEdificio + " " + ubicacionNumero;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion == null ? null : ubicacion.trim();
        this.ubicacionEdificio = null;
        this.ubicacionNumero = null;
        if (this.ubicacion != null) {
            String[] partes = this.ubicacion.split("\\s+", 2);
            if (partes.length == 2) {
                this.ubicacionEdificio = partes[0];
                this.ubicacionNumero = partes[1];
            }
        }
    }

    public String[] getHorarioDias1() { return horarioDias1; }

    public void setHorarioDias1(String[] horarioDias1) { this.horarioDias1 = horarioDias1 == null ? new String[]{""} : horarioDias1; }

    public String[] getHorarioInicios1() { return horarioInicios1; }

    public void setHorarioInicios1(String[] horarioInicios1) { this.horarioInicios1 = horarioInicios1 == null ? new String[]{""} : horarioInicios1; }

    public String[] getHorarioFines1() { return horarioFines1; }

    public void setHorarioFines1(String[] horarioFines1) { this.horarioFines1 = horarioFines1 == null ? new String[]{""} : horarioFines1; }

    public String getUbicacionEdificio() { return ubicacionEdificio; }

    public void setUbicacionEdificio(String ubicacionEdificio) { this.ubicacionEdificio = ubicacionEdificio; }

    public String getUbicacionNumero() { return ubicacionNumero; }

    public void setUbicacionNumero(String ubicacionNumero) { this.ubicacionNumero = ubicacionNumero; }

    @AssertTrue(message = "Completa al menos un horario con día y horas válidas")
    public boolean isHorarioValido() {
        if (horarioDias1 == null || horarioDias1.length == 0) return false;
        boolean tieneHorario = false;
        for (int indice = 0; indice < horarioDias1.length; indice++) {
            String dia = horarioDias1[indice];
            String inicio = valorEn(horarioInicios1, indice);
            String fin = valorEn(horarioFines1, indice);
            if (!esHorarioValido(dia, inicio, fin)) return false;
            tieneHorario = true;
        }
        return tieneHorario;
    }

    @AssertTrue(message = "La ubicación debe tener un edificio válido y exactamente 3 dígitos")
    public boolean isUbicacionValida() {
        return ubicacionEdificio != null
                && ubicacionEdificio.matches("CIT|[ABDEFGHIJK]")
                && ubicacionNumero != null
                && ubicacionNumero.matches("\\d{3}");
    }

    private boolean esHorarioValido(String dia, String inicio, String fin) {
        return dia != null && !dia.isBlank()
                && inicio != null && inicio.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")
                && fin != null && fin.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")
                && inicio.compareTo(fin) < 0;
    }

    private String valorEn(String[] valores, int indice) {
        return valores != null && indice < valores.length && valores[indice] != null ? valores[indice] : "";
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
