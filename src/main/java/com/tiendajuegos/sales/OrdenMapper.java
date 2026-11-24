package com.tiendajuegos.sales;

import com.tiendajuegos.dto.ApiDtos.DetalleOrdenResponse;
import com.tiendajuegos.dto.ApiDtos.OrdenResponse;
import com.tiendajuegos.inventory.ClaveDigitalRepository;
import com.tiendajuegos.inventory.KeyCipherService;
import java.util.List;

/** Convierte ordenes a respuestas, entregando la clave solo cuando el pago fue aprobado. */
public final class OrdenMapper {

    private OrdenMapper() {
    }

    public static OrdenResponse toResponse(Orden orden,
                                           List<DetalleOrden> detalles,
                                           ClaveDigitalRepository claves,
                                           KeyCipherService cipher,
                                           boolean incluirClaves) {
        String cupon = orden.getCupon() == null ? null : orden.getCupon().getCodigo();
        List<DetalleOrdenResponse> items = detalles.stream().map(detalle -> {
            String clave = null;
            if (incluirClaves && orden.getEstadoPago() == EstadoPago.pagado) {
                clave = claves.findById(detalle.getClaveId())
                        .map(registro -> cipher.descifrar(registro.getClaveCifrada()))
                        .orElse(null);
            }
            return new DetalleOrdenResponse(detalle.getJuego().getId(),
                    detalle.getJuego().getTitulo(), detalle.getPrecioUnitario(), clave);
        }).toList();
        return new OrdenResponse(orden.getId(), orden.getFechaCreacion(), orden.getTotal(),
                orden.getEstadoPago().name(), cupon, items);
    }
}
