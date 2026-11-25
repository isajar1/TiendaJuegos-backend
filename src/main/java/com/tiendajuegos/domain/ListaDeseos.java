package com.tiendajuegos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "listas_deseos")
public class ListaDeseos {

    @EmbeddedId
    private ListaDeseosId id;

    @Column(name = "fecha_agregado", nullable = false, updatable = false)
    private Instant fechaAgregado = Instant.now();

    protected ListaDeseos() {
    }

    public ListaDeseos(Long usuarioId, Long juegoId) {
        this.id = new ListaDeseosId(usuarioId, juegoId);
    }

    public ListaDeseosId getId() { return id; }
    public Instant getFechaAgregado() { return fechaAgregado; }
}
