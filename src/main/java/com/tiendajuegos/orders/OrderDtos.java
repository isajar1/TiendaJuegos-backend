package com.tiendajuegos.orders;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record PagoRequest(Boolean aprobar) {
    }

    public record EstadoRequest(@NotBlank String estadoPago) {
    }

    public record OrdenItemResponse(
            Long juegoId,
            String titulo,
            BigDecimal precioUnitario,
            String clave) {
    }

    public record OrdenResponse(
            Long id,
            String fechaCreacion,
            BigDecimal total,
            String estadoPago,
            String cupon,
            List<OrdenItemResponse> items) {
    }
}
