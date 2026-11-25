package com.tiendajuegos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "logs_auditoria")
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false, length = 120)
    private String accion;

    @Column(nullable = false, length = 80)
    private String entidad;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private Instant fechaHora = Instant.now();

    protected LogAuditoria() {
    }

    public LogAuditoria(Long usuarioId, String accion, String entidad, Instant fechaHora) {
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.entidad = entidad;
        this.fechaHora = fechaHora;
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getAccion() { return accion; }
    public String getEntidad() { return entidad; }
    public Instant getFechaHora() { return fechaHora; }
}
