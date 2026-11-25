package com.tiendajuegos.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/** DTOs públicos del catálogo. */
public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record JuegoRequest(
            @NotBlank String titulo,
            String descripcion,
            @NotNull @DecimalMin("0") BigDecimal precio,
            @NotBlank String region,
            @NotNull Long plataformaId,
            @NotNull Long categoriaId) {
    }

    public record JuegoResponse(
            Long id,
            String titulo,
            String descripcion,
            BigDecimal precio,
            String region,
            String plataforma,
            String categoria,
            boolean activo,
            long clavesDisponibles,
            double calificacionPromedio,
            long totalResenas) {
    }

    public record NombreRequest(@NotBlank @Size(max = 80) String nombre) {
    }

    public record NombreResponse(Long id, String nombre) {
    }

    public record ClavesRequest(@NotEmpty List<@NotBlank String> claves) {
    }

    public record ClavesResponse(long creadas) {
    }

    public record ClavesDisponiblesResponse(long disponibles, long vendidas) {
    }
}
