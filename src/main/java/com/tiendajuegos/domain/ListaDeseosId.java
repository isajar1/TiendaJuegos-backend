package com.tiendajuegos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ListaDeseosId implements Serializable {

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "juego_id", nullable = false)
    private Long juegoId;

    protected ListaDeseosId() {
    }

    public ListaDeseosId(Long usuarioId, Long juegoId) {
        this.usuarioId = usuarioId;
        this.juegoId = juegoId;
    }

    public Long getUsuarioId() { return usuarioId; }
    public Long getJuegoId() { return juegoId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ListaDeseosId that)) return false;
        return Objects.equals(usuarioId, that.usuarioId) && Objects.equals(juegoId, that.juegoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId, juegoId);
    }
}
