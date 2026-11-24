package com.tiendajuegos.inventory;

import com.tiendajuegos.catalog.Juego;
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

@Entity
@Table(name = "claves_digitales")
public class ClaveDigital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;

    @Column(name = "clave_cifrada", nullable = false, columnDefinition = "TEXT")
    private String claveCifrada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoClave estado = EstadoClave.disponible;

    @Column(name = "orden_id")
    private Long ordenId;

    protected ClaveDigital() {
    }

    public ClaveDigital(Juego juego, String claveCifrada) {
        this.juego = juego;
        this.claveCifrada = claveCifrada;
    }

    public Long getId() { return id; }
    public Juego getJuego() { return juego; }
    public String getClaveCifrada() { return claveCifrada; }
    public EstadoClave getEstado() { return estado; }
    public Long getOrdenId() { return ordenId; }

    public void vender(Long ordenId) {
        this.estado = EstadoClave.vendida;
        this.ordenId = ordenId;
    }
}
