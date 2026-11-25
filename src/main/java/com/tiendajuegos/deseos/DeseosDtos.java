package com.tiendajuegos.deseos;

import java.math.BigDecimal;
import java.util.List;

public final class DeseosDtos {

    private DeseosDtos() {
    }

    public record DeseoResponse(Long juegoId, String titulo, BigDecimal precio) {
    }

    /** `creado` sólo decide el código HTTP; el cuerpo de la API es siempre la lista. */
    public record DeseosResult(boolean creado, List<DeseoResponse> deseos) {
    }
}
