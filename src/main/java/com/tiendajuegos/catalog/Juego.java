package com.tiendajuegos.catalog;

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
import java.time.LocalDateTime;

@Entity
@Table(name = "juegos")
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false, length = 80)
    private String region = "Global";

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plataforma_id", nullable = false)
    private Plataforma plataforma;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoJuego estado = EstadoJuego.activo;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected Juego() {
    }

    public Juego(String titulo, String descripcion, BigDecimal precio, String region,
                 Plataforma plataforma, Categoria categoria) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.region = region;
        this.plataforma = plataforma;
        this.categoria = categoria;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public String getRegion() { return region; }
    public Plataforma getPlataforma() { return plataforma; }
    public Categoria getCategoria() { return categoria; }
    public EstadoJuego getEstado() { return estado; }
    public LocalDateTime getCreadoEn() { return creadoEn; }

    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public void setRegion(String region) { this.region = region; }
    public void setPlataforma(Plataforma plataforma) { this.plataforma = plataforma; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public void setEstado(EstadoJuego estado) { this.estado = estado; }
}
