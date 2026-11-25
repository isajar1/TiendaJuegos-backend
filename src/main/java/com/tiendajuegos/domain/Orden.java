package com.tiendajuegos.domain;

import com.tiendajuegos.user.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ordenes")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cupon_id")
    private Cupon cupon;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 20)
    private EstadoPago estadoPago = EstadoPago.pendiente;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion = Instant.now();

    protected Orden() {
    }

    public Orden(Usuario usuario, Cupon cupon, BigDecimal total, EstadoPago estadoPago) {
        this.usuario = usuario;
        this.cupon = cupon;
        this.total = total;
        this.estadoPago = estadoPago;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Cupon getCupon() { return cupon; }
    public BigDecimal getTotal() { return total; }
    public EstadoPago getEstadoPago() { return estadoPago; }
    public Instant getFechaCreacion() { return fechaCreacion; }

    public void setEstadoPago(EstadoPago estadoPago) { this.estadoPago = estadoPago; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
