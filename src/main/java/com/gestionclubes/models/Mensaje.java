package com.gestionclubes.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Mensaje privado enviado entre dos amistades aceptadas. */
@Entity
@Table(name = "mensajes")
public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id", nullable = false)
    private Usuario remitente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @Column(nullable = false, length = 1000)
    private String contenido;

    @Column(nullable = false, updatable = false)
    private LocalDateTime enviadoEn;

    @Column(nullable = false)
    private boolean leido = false;

    @PrePersist
    void registrarFecha() {
        enviadoEn = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Usuario getRemitente() { return remitente; }
    public void setRemitente(Usuario remitente) { this.remitente = remitente; }
    public Usuario getDestinatario() { return destinatario; }
    public void setDestinatario(Usuario destinatario) { this.destinatario = destinatario; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public LocalDateTime getEnviadoEn() { return enviadoEn; }
    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }
}
