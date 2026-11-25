package com.tiendajuegos.cart;

import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {

    private CartDtos() {
    }

    public record CarritoItemResponse(Long juegoId, String titulo, BigDecimal precio) {
    }

    public record CarritoResponse(
            List<CarritoItemResponse> items,
            BigDecimal subtotal,
            BigDecimal descuento,
            BigDecimal total,
            String cupon) {
    }
}
