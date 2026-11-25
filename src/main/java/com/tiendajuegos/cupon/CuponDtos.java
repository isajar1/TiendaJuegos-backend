package com.tiendajuegos.cupon;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class CuponDtos {

    private CuponDtos() {
    }

    public record CuponRequest(
            @NotBlank String codigo,
            @NotNull @DecimalMin("0.01") @DecimalMax("100") BigDecimal porcentajeDescuento,
            @NotBlank String fechaExpiracion,
            Boolean activo) {
    }

    public record CuponResponse(
            Long id,
            String codigo,
            BigDecimal porcentajeDescuento,
            String fechaExpiracion,
            boolean activo) {
    }

    public record ActivoRequest(@NotNull Boolean activo) {
    }

    public record CodigoRequest(@NotBlank String codigo) {
    }
}
