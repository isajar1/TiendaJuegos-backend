package com.tiendajuegos.cart;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.cart.CartDtos.CarritoItemResponse;
import com.tiendajuegos.cart.CartDtos.CarritoResponse;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.cupon.CuponDtos.CodigoRequest;
import com.tiendajuegos.cupon.CuponService;
import com.tiendajuegos.domain.Carrito;
import com.tiendajuegos.domain.CarritoItem;
import com.tiendajuegos.domain.Cupon;
import com.tiendajuegos.domain.EstadoClave;
import com.tiendajuegos.domain.Juego;
import com.tiendajuegos.repository.CarritoItemRepository;
import com.tiendajuegos.repository.CarritoRepository;
import com.tiendajuegos.repository.ClaveDigitalRepository;
import com.tiendajuegos.repository.JuegoRepository;
import com.tiendajuegos.user.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarritoService {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository itemRepository;
    private final JuegoRepository juegoRepository;
    private final CuponService cuponService;
    private final ClaveDigitalRepository claveRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditService auditService;

    public CarritoService(CarritoRepository carritoRepository, CarritoItemRepository itemRepository,
                          JuegoRepository juegoRepository, CuponService cuponService,
                          ClaveDigitalRepository claveRepository, UsuarioRepository usuarioRepository,
                          AuditService auditService) {
        this.carritoRepository = carritoRepository;
        this.itemRepository = itemRepository;
        this.juegoRepository = juegoRepository;
        this.cuponService = cuponService;
        this.claveRepository = claveRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public CarritoResponse obtener(Long usuarioId) {
        return resumen(carritoRepository.findByUsuarioId(usuarioId).orElse(null));
    }

    @Transactional
    public CarritoResponse agregar(Long usuarioId, Long juegoId) {
        Juego juego = juegoRepository.findById(juegoId)
                .orElseThrow(() -> ApiException.notFound("Juego no encontrado"));
        Carrito carrito = asegurarCarrito(usuarioId);
        if (itemRepository.findByCarritoIdAndJuegoId(carrito.getId(), juegoId).isPresent()) {
            return resumen(carrito);
        }
        if (!juego.isActivo()) {
            throw ApiException.badRequest("El juego no está disponible");
        }
        if (claveRepository.countByJuegoIdAndEstado(juegoId, EstadoClave.disponible) == 0) {
            throw ApiException.conflict("No hay claves disponibles para " + juego.getTitulo());
        }
        itemRepository.save(new CarritoItem(carrito, juego));
        auditService.registrar(usuarioId, "juego agregado al carrito", "carrito");
        return resumen(carrito);
    }

    @Transactional
    public CarritoResponse eliminarItem(Long usuarioId, Long juegoId) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId).orElse(null);
        if (carrito != null) {
            Optional<CarritoItem> item = itemRepository.findByCarritoIdAndJuegoId(carrito.getId(), juegoId);
            if (item.isPresent()) {
                itemRepository.delete(item.get());
                auditService.registrar(usuarioId, "juego eliminado del carrito", "carrito");
            }
        }
        return resumen(carrito);
    }

    @Transactional
    public void vaciar(Long usuarioId) {
        carritoRepository.findByUsuarioId(usuarioId).ifPresent(carrito -> {
            itemRepository.deleteAll(itemRepository.findByCarritoIdOrderByIdAsc(carrito.getId()));
            carrito.setCupon(null);
            carritoRepository.save(carrito);
        });
        auditService.registrar(usuarioId, "carrito vaciado", "carrito");
    }

    @Transactional
    public CarritoResponse aplicarCupon(Long usuarioId, CodigoRequest request) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> ApiException.badRequest("El carrito está vacío"));
        List<CarritoItem> items = itemRepository.findByCarritoIdOrderByIdAsc(carrito.getId());
        if (items.isEmpty()) {
            throw ApiException.badRequest("El carrito está vacío");
        }
        Cupon cupon = cuponService.validar(request.codigo());
        carrito.setCupon(cupon);
        carritoRepository.save(carrito);
        auditService.registrar(usuarioId, "cupón aplicado al carrito", "carrito");
        return resumen(carrito);
    }

    /** Arma la respuesta con los items actualmente persistidos. */
    @Transactional(readOnly = true)
    public CarritoResponse resumen(Carrito carrito) {
        if (carrito == null) {
            return new CarritoResponse(List.of(), escalar(BigDecimal.ZERO), escalar(BigDecimal.ZERO),
                    escalar(BigDecimal.ZERO), null);
        }
        List<CarritoItemResponse> items = itemRepository.findByCarritoIdOrderByIdAsc(carrito.getId()).stream()
                .map(item -> new CarritoItemResponse(item.getJuego().getId(), item.getJuego().getTitulo(),
                        item.getJuego().getPrecio()))
                .toList();
        BigDecimal subtotal = items.stream()
                .map(CarritoItemResponse::precio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descuento = carrito.getCupon() == null ? BigDecimal.ZERO
                : subtotal.multiply(cuponService.porcentaje(carrito.getCupon()))
                        .divide(CIEN, 2, RoundingMode.HALF_UP);
        return new CarritoResponse(items, escalar(subtotal), escalar(descuento),
                escalar(subtotal.subtract(descuento)),
                carrito.getCupon() == null ? null : carrito.getCupon().getCodigo());
    }

    private Carrito asegurarCarrito(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId).orElseGet(() ->
                carritoRepository.save(new Carrito(usuarioRepository.getReferenceById(usuarioId))));
    }

    private BigDecimal escalar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
