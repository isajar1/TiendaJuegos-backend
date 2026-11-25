package com.tiendajuegos.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "carrito_items", uniqueConstraints = @UniqueConstraint(
        name = "uq_carrito_item", columnNames = {"carrito_id", "juego_id"}))
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;

    protected CarritoItem() {
    }

    public CarritoItem(Carrito carrito, Juego juego) {
        this.carrito = carrito;
        this.juego = juego;
    }

    public Long getId() { return id; }
    public Carrito getCarrito() { return carrito; }
    public Juego getJuego() { return juego; }
}
