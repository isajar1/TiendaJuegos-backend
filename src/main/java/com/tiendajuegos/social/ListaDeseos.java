package com.tiendajuegos.social;

import com.tiendajuegos.catalog.Juego;
import com.tiendajuegos.user.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "listas_deseos")
public class ListaDeseos {

    @EmbeddedId
    private ListaDeseosId id;

    @MapsId("usuarioId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @MapsId("juegoId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "juego_id")
    private Juego juego;

    @Column(name = "fecha_agregado", nullable = false, updatable = false)
    private LocalDateTime fechaAgregado = LocalDateTime.now();

    protected ListaDeseos() {
    }

    public ListaDeseos(Usuario usuario, Juego juego) {
        this.usuario = usuario;
        this.juego = juego;
    }

    public ListaDeseosId getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Juego getJuego() { return juego; }
    public LocalDateTime getFechaAgregado() { return fechaAgregado; }
}
