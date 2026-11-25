package com.tiendajuegos.orders;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.crypto.CryptoService;
import com.tiendajuegos.domain.DetalleOrden;
import com.tiendajuegos.domain.EstadoPago;
import com.tiendajuegos.domain.Orden;
import com.tiendajuegos.orders.OrderDtos.EstadoRequest;
import com.tiendajuegos.orders.OrderDtos.OrdenItemResponse;
import com.tiendajuegos.orders.OrderDtos.OrdenResponse;
import com.tiendajuegos.repository.DetalleOrdenRepository;
import com.tiendajuegos.repository.OrdenRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleRepository;
    private final CryptoService cryptoService;
    private final AuditService auditService;

    public OrdenService(OrdenRepository ordenRepository, DetalleOrdenRepository detalleRepository,
                        CryptoService cryptoService, AuditService auditService) {
        this.ordenRepository = ordenRepository;
        this.detalleRepository = detalleRepository;
        this.cryptoService = cryptoService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> mias(Long usuarioId) {
        return ordenRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> todas() {
        return ordenRepository.findAllByOrderByFechaCreacionDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdenResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public OrdenResponse obtenerMia(Long usuarioId, Long id) {
        Orden orden = buscar(id);
        if (!orden.getUsuario().getId().equals(usuarioId)) {
            throw ApiException.notFound("Orden no encontrada");
        }
        return toResponse(orden);
    }

    @Transactional
    public OrdenResponse cambiarEstado(Long id, EstadoRequest request, Long usuarioId) {
        Orden orden = buscar(id);
        EstadoPago estado;
        try {
            estado = EstadoPago.valueOf(request.estadoPago().trim().toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw ApiException.badRequest("Estado de pago inválido");
        }
        orden.setEstadoPago(estado);
        Orden guardada = ordenRepository.save(orden);
        auditService.registrar(usuarioId, "estado de orden actualizado", "orden");
        return toResponse(guardada);
    }

    private Orden buscar(Long id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Orden no encontrada"));
    }

    private OrdenResponse toResponse(Orden orden) {
        List<OrdenItemResponse> items = detalleRepository.findByOrdenIdOrderByIdAsc(orden.getId()).stream()
                .map(this::toItem)
                .toList();
        return new OrdenResponse(orden.getId(), orden.getFechaCreacion().toString(), orden.getTotal(),
                orden.getEstadoPago().name(),
                orden.getCupon() == null ? null : orden.getCupon().getCodigo(), items);
    }

    private OrdenItemResponse toItem(DetalleOrden detalle) {
        return new OrdenItemResponse(detalle.getJuego().getId(), detalle.getJuego().getTitulo(),
                detalle.getPrecioUnitario(), cryptoService.decrypt(detalle.getClave().getClaveCifrada()));
    }
}
