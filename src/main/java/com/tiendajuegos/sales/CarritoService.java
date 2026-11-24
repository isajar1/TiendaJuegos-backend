package com.tiendajuegos.sales;

import com.tiendajuegos.catalog.EstadoJuego;
import com.tiendajuegos.catalog.Juego;
import com.tiendajuegos.catalog.JuegoRepository;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.dto.ApiDtos.CarritoItemResponse;
import com.tiendajuegos.dto.ApiDtos.CarritoRequest;
import com.tiendajuegos.dto.ApiDtos.CarritoResponse;
import com.tiendajuegos.dto.ApiDtos.ItemCarritoRequest;
import com.tiendajuegos.inventory.ClaveDigitalRepository;
import com.tiendajuegos.inventory.EstadoClave;
import com.tiendajuegos.promotions.Cupon;
import com.tiendajuegos.promotions.CuponService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CarritoService {

    private final JuegoRepository juegoRepository;
    private final ClaveDigitalRepository claveRepository;
    private final CuponService cuponService;

    public CarritoService(JuegoRepository juegoRepository,
                          ClaveDigitalRepository claveRepository,
                          CuponService cuponService) {
        this.juegoRepository = juegoRepository;
        this.claveRepository = claveRepository;
        this.cuponService = cuponService;
    }

    public CarritoResponse calcular(CarritoRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El carrito esta vacio.");
        }
        List<CarritoItemResponse> items = new ArrayList<>();
        Set<Long> vistos = new HashSet<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (ItemCarritoRequest item : request.items()) {
            if (item == null || item.juegoId() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Cada item debe indicar un juego.");
            }
            if (!vistos.add(item.juegoId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes agregar el mismo juego dos veces.");
            }
            Juego juego = juegoRepository.findById(item.juegoId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Juego no encontrado."));
            if (juego.getEstado() != EstadoJuego.activo) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "El juego " + juego.getTitulo() + " no esta disponible.");
            }
            if (claveRepository.countByJuegoIdAndEstado(juego.getId(), EstadoClave.disponible) == 0) {
                throw new ApiException(HttpStatus.CONFLICT, "No hay claves disponibles para " + juego.getTitulo() + ".");
            }
            items.add(new CarritoItemResponse(juego.getId(), juego.getTitulo(), juego.getPrecio(), true));
            subtotal = subtotal.add(juego.getPrecio());
        }

        BigDecimal descuento = BigDecimal.ZERO;
        String codigoCupon = null;
        if (request.codigoCupon() != null && !request.codigoCupon().isBlank()) {
            Cupon cupon = cuponService.validar(request.codigoCupon());
            descuento = cuponService.descuento(subtotal, cupon);
            codigoCupon = cupon.getCodigo();
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(descuento).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return new CarritoResponse(items, subtotal, descuento, total, codigoCupon);
    }
}
