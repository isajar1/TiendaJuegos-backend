package com.tiendajuegos.reportes;

import java.math.BigDecimal;

public final class ReportesDtos {

    private ReportesDtos() {
    }

    public record ReporteVentaResponse(
            Long juegoId, String titulo, long unidades, BigDecimal ingresos) {
    }

    public record ReporteClienteResponse(
            Long usuarioId, String nombre, long ordenes, BigDecimal total) {
    }
}
