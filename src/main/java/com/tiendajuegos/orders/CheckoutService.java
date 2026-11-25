package com.tiendajuegos.orders;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.cart.CarritoService;
import com.tiendajuegos.cart.CartDtos.CarritoResponse;
import com.tiendajuegos.cart.CartDtos.CarritoItemResponse;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.crypto.CryptoService;
import com.tiendajuegos.domain.Carrito;
import com.tiendajuegos.domain.CarritoItem;
import com.tiendajuegos.domain.ClaveDigital;
import com.tiendajuegos.domain.DetalleOrden;
import com.tiendajuegos.domain.EstadoClave;
import com.tiendajuegos.domain.EstadoPago;
import com.tiendajuegos.domain.Orden;
import com.tiendajuegos.orders.OrderDtos.OrdenItemResponse;
import com.tiendajuegos.orders.OrderDtos.OrdenResponse;
import com.tiendajuegos.repository.CarritoItemRepository;
import com.tiendajuegos.repository.CarritoRepository;
import com.tiendajuegos.repository.ClaveDigitalRepository;
import com.tiendajuegos.repository.DetalleOrdenRepository;
import com.tiendajuegos.repository.OrdenRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutService {

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository itemRepository;
    private final CarritoService carritoService;
    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleRepository;
    private final ClaveDigitalRepository claveRepository;
    private final CryptoService cryptoService;
    private final AuditService auditService;

    public CheckoutService(CarritoRepository carritoRepository, CarritoItemRepository itemRepository,
                           CarritoService carritoService, OrdenRepository ordenRepository,
                           DetalleOrdenRepository detalleRepository, ClaveDigitalRepository claveRepository,
                           CryptoService cryptoService, AuditService auditService) {
        this.carritoRepository = carritoRepository;
        this.itemRepository = itemRepository;
        this.carritoService = carritoService;
        this.ordenRepository = ordenRepository;
        this.detalleRepository = detalleRepository;
        this.claveRepository = claveRepository;
        this.cryptoService = cryptoService;
        this.auditService = auditService;
    }

    @Transactional
    public OrdenResponse pagar(Long usuarioId, boolean aprobar) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> ApiException.badRequest("El carrito está vacío"));
        List<CarritoItem> items = itemRepository.findByCarritoIdOrderByIdAsc(carrito.getId());
        CarritoResponse resumen = carritoService.resumen(carrito);
        if (items.isEmpty()) {
            throw ApiException.badRequest("El carrito está vacío");
        }

        if (!aprobar) {
            Orden rechazada = ordenRepository.save(new Orden(carrito.getUsuario(), carrito.getCupon(),
                    resumen.total(), EstadoPago.rechazado));
            auditService.registrar(usuarioId, "pago rechazado", "orden");
            return new OrdenResponse(rechazada.getId(), rechazada.getFechaCreacion().toString(),
                    rechazada.getTotal(), rechazada.getEstadoPago().name(), cuponCode(carrito),
                    resumen.items().stream()
                            .map(item -> new OrdenItemResponse(item.juegoId(), item.titulo(), item.precio(), ""))
                            .toList());
        }

        for (CarritoItem item : items) {
            if (claveRepository.countByJuegoIdAndEstado(item.getJuego().getId(), EstadoClave.disponible) < 1) {
                throw ApiException.conflict("No hay claves disponibles para " + item.getJuego().getTitulo());
            }
        }

        Orden orden = ordenRepository.save(new Orden(carrito.getUsuario(), carrito.getCupon(),
                resumen.total(), EstadoPago.pagado));
        List<DetalleOrden> detalles = new ArrayList<>();
        List<String> clavesPlanas = new ArrayList<>();
        for (CarritoItem item : items) {
            ClaveDigital clave = claveRepository
                    .findFirstByJuegoIdAndEstadoOrderByIdAsc(item.getJuego().getId(), EstadoClave.disponible)
                    .orElseThrow(() -> ApiException.conflict(
                            "No hay claves disponibles para " + item.getJuego().getTitulo()));
            clave.setEstado(EstadoClave.vendida);
            clave.setOrden(orden);
            claveRepository.save(clave);
            detalles.add(new DetalleOrden(orden, item.getJuego(), item.getJuego().getPrecio(), clave));
            clavesPlanas.add(cryptoService.decrypt(clave.getClaveCifrada()));
            auditService.registrar(usuarioId, "clave entregada", "clave_digital");
        }
        detalleRepository.saveAll(detalles);
        carritoService.vaciar(usuarioId);
        auditService.registrar(usuarioId, "pago aprobado", "orden");
        return toResponse(orden, detalles, clavesPlanas);
    }

    private OrdenResponse toResponse(Orden orden, List<DetalleOrden> detalles, List<String> clavesPlanas) {
        List<OrdenItemResponse> items = new ArrayList<>();
        for (int i = 0; i < detalles.size(); i++) {
            DetalleOrden detalle = detalles.get(i);
            items.add(new OrdenItemResponse(detalle.getJuego().getId(), detalle.getJuego().getTitulo(),
                    detalle.getPrecioUnitario(), clavesPlanas.get(i)));
        }
        return new OrdenResponse(orden.getId(), orden.getFechaCreacion().toString(), orden.getTotal(),
                orden.getEstadoPago().name(), cuponCode(orden), items);
    }

    private String cuponCode(Orden orden) {
        return orden.getCupon() == null ? null : orden.getCupon().getCodigo();
    }

    private String cuponCode(Carrito carrito) {
        return carrito.getCupon() == null ? null : carrito.getCupon().getCodigo();
    }
}
