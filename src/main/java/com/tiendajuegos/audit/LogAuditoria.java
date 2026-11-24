package com.tiendajuegos.audit;

import com.tiendajuegos.user.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "logs_auditoria")
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 120)
    private String accion;

    @Column(nullable = false, length = 80)
    private String entidad;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    protected LogAuditoria() {
    }

    public LogAuditoria(Usuario usuario, String accion, String entidad) {
        this.usuario = usuario;
        this.accion = accion;
        this.entidad = entidad;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getAccion() { return accion; }
    public String getEntidad() { return entidad; }
    public LocalDateTime getFechaHora() { return fechaHora; }
}
