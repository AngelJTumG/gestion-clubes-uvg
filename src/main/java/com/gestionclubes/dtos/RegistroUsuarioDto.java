package com.gestionclubes.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class RegistroUsuarioDto {
    @NotBlank(message = "Por favor, ingresa tu carné")
    @Pattern(regexp = "[0-9]{1,30}", message = "El carné debe contener de 1 a 30 dígitos")
    private String carne;

    @NotBlank(message = "Por favor, ingresa tu nombre")
    @Size(max = 100, message = "Tu nombre puede tener hasta 100 caracteres")
    private String nombre;

    @NotBlank(message = "Por favor, ingresa tu correo")
    @Email(message = "Ingresa un correo electrónico válido")
    @Size(max = 150, message = "Tu correo puede tener hasta 150 caracteres")
    private String email;

    @NotBlank(message = "Por favor, ingresa una contraseña")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String password;

    @NotBlank(message = "Por favor, confirma tu contraseña")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String confirmarPassword;

    public String getCarne() { return carne; }
    public void setCarne(String carne) { this.carne = carne == null ? null : carne.trim(); }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmarPassword() { return confirmarPassword; }
    public void setConfirmarPassword(String confirmarPassword) { this.confirmarPassword = confirmarPassword; }
}

