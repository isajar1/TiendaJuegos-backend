package com.tiendajuegos.resena;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ResenaDtos {

    private ResenaDtos() {
    }

    public record ResenaRequest(
            @NotNull @Min(1) @Max(5) Integer calificacion,
            @Size(max = 1000) String comentario) {
    }

    public record ResenaResponse(
            Long id,
            Long juegoId,
            Long usuarioId,
            String nombreUsuario,
            int calificacion,
            String comentario,
            String fecha) {
    }
}
