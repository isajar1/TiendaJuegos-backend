package com.tiendajuegos.domain;

import com.tiendajuegos.user.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "resenas")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private int calificacion;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(nullable = false, updatable = false)
    private Instant fecha = Instant.now();

    protected Resena() {
    }

    public Resena(Juego juego, Usuario usuario, int calificacion, String comentario) {
        this.juego = juego;
        this.usuario = usuario;
        this.calificacion = calificacion;
        this.comentario = comentario;
    }

    public Long getId() { return id; }
    public Juego getJuego() { return juego; }
    public Usuario getUsuario() { return usuario; }
    public int getCalificacion() { return calificacion; }
    public String getComentario() { return comentario; }
    public Instant getFecha() { return fecha; }
}
