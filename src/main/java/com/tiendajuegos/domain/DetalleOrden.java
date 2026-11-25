package com.tiendajuegos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_orden")
public class DetalleOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clave_id", nullable = false, unique = true)
    private ClaveDigital clave;

    protected DetalleOrden() {
    }

    public DetalleOrden(Orden orden, Juego juego, BigDecimal precioUnitario, ClaveDigital clave) {
        this.orden = orden;
        this.juego = juego;
        this.precioUnitario = precioUnitario;
        this.clave = clave;
    }

    public Long getId() { return id; }
    public Orden getOrden() { return orden; }
    public Juego getJuego() { return juego; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public ClaveDigital getClave() { return clave; }
}
