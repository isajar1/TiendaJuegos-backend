package com.tiendajuegos.sales;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.catalog.JuegoRepository;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUser;
import com.tiendajuegos.dto.ApiDtos.CarritoItemResponse;
import com.tiendajuegos.dto.ApiDtos.CarritoRequest;
import com.tiendajuegos.dto.ApiDtos.CarritoResponse;
import com.tiendajuegos.dto.ApiDtos.CheckoutRequest;
import com.tiendajuegos.dto.ApiDtos.OrdenResponse;
import com.tiendajuegos.inventory.ClaveDigital;
import com.tiendajuegos.inventory.ClaveDigitalRepository;
import com.tiendajuegos.inventory.EstadoClave;
import com.tiendajuegos.inventory.KeyCipherService;
import com.tiendajuegos.promotions.Cupon;
import com.tiendajuegos.promotions.CuponRepository;
import com.tiendajuegos.user.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutService {

    private final CarritoService carritoService;
    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleRepository;
    private final JuegoRepository juegoRepository;
    private final ClaveDigitalRepository claveRepository;
    private final CuponRepository cuponRepository;
    private final KeyCipherService cipher;
    private final CurrentUser currentUser;
    private final AuditService auditService;

    public CheckoutService(CarritoService carritoService,
                           OrdenRepository ordenRepository,
                           DetalleOrdenRepository detalleRepository,
                           JuegoRepository juegoRepository,
                           ClaveDigitalRepository claveRepository,
                           CuponRepository cuponRepository,
                           KeyCipherService cipher,
                           CurrentUser currentUser,
                           AuditService auditService) {
        this.carritoService = carritoService;
        this.ordenRepository = ordenRepository;
        this.detalleRepository = detalleRepository;
        this.juegoRepository = juegoRepository;
        this.claveRepository = claveRepository;
        this.cuponRepository = cuponRepository;
        this.cipher = cipher;
        this.currentUser = currentUser;
        this.auditService = auditService;
    }

    @Transactional
    public OrdenResponse pagar(CheckoutRequest request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La peticion de pago no es valida.");
        }
        CarritoResponse carrito = carritoService.calcular(
                new CarritoRequest(request.items(), request.codigoCupon()));
        Usuario usuario = currentUser.usuario();
        Cupon cupon = carrito.cupon() == null ? null
                : cuponRepository.findByCodigoIgnoreCase(carrito.cupon()).orElse(null);
        boolean aprobado = request.aprobarPago() == null || request.aprobarPago();

        Orden orden = new Orden(usuario, cupon, carrito.total());
        orden.setEstadoPago(aprobado ? EstadoPago.pagado : EstadoPago.rechazado);
        Orden guardada = ordenRepository.save(orden);

        if (aprobado) {
            for (CarritoItemResponse item : carrito.items()) {
                ClaveDigital clave = claveRepository
                        .findFirstByJuegoIdAndEstado(item.juegoId(), EstadoClave.disponible)
                        .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT,
                                "Ya no hay claves disponibles para " + item.titulo() + "."));
                clave.vender(guardada.getId());
                claveRepository.save(clave);
                detalleRepository.save(new DetalleOrden(guardada,
                        juegoRepository.getReferenceById(item.juegoId()), item.precioUnitario(), clave.getId()));
            }
            auditService.registrar("PAGO_ORDEN", "ordenes");
            auditService.registrar("ENTREGA_CLAVE", "claves_digitales");
        } else {
            auditService.registrar("PAGO_RECHAZADO", "ordenes");
        }
        return OrdenMapper.toResponse(guardada, detalleRepository.findByOrdenId(guardada.getId()),
                claveRepository, cipher, true);
    }
}
