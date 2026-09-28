package com.gestionclubes.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistroUsuarioDto {
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

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmarPassword() { return confirmarPassword; }
    public void setConfirmarPassword(String confirmarPassword) { this.confirmarPassword = confirmarPassword; }
}

