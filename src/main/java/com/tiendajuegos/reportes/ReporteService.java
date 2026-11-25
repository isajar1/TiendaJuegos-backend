package com.tiendajuegos.reportes;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.domain.DetalleOrden;
import com.tiendajuegos.domain.EstadoPago;
import com.tiendajuegos.domain.Orden;
import com.tiendajuegos.reportes.ReportesDtos.ReporteClienteResponse;
import com.tiendajuegos.reportes.ReportesDtos.ReporteVentaResponse;
import com.tiendajuegos.repository.DetalleOrdenRepository;
import com.tiendajuegos.repository.OrdenRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteService {

    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleRepository;
    private final AuditService auditService;

    public ReporteService(OrdenRepository ordenRepository, DetalleOrdenRepository detalleRepository,
                          AuditService auditService) {
        this.ordenRepository = ordenRepository;
        this.detalleRepository = detalleRepository;
        this.auditService = auditService;
    }

    @Transactional
    public List<ReporteVentaResponse> ventasPorJuego(String desde, String hasta, Long usuarioId) {
        List<Orden> ordenes = ordenesPagadas(desde, hasta);
        Map<Long, ReporteVentaResponse> agrupado = new LinkedHashMap<>();
        for (Orden orden : ordenes) {
            for (DetalleOrden detalle : detalleRepository.findByOrdenIdOrderByIdAsc(orden.getId())) {
                Long juegoId = detalle.getJuego().getId();
                ReporteVentaResponse actual = agrupado.get(juegoId);
                if (actual == null) {
                    agrupado.put(juegoId, new ReporteVentaResponse(juegoId, detalle.getJuego().getTitulo(),
                            1, detalle.getPrecioUnitario()));
                } else {
                    agrupado.put(juegoId, new ReporteVentaResponse(juegoId, actual.titulo(),
                            actual.unidades() + 1, actual.ingresos().add(detalle.getPrecioUnitario())));
                }
            }
        }
        auditService.registrar(usuarioId, "reporte de ventas generado", "reporte");
        return new ArrayList<>(agrupado.values());
    }

    @Transactional
    public List<ReporteClienteResponse> comprasPorCliente(String desde, String hasta, Long usuarioId) {
        List<Orden> ordenes = ordenesPagadas(desde, hasta);
        Map<Long, ReporteClienteResponse> agrupado = new LinkedHashMap<>();
        for (Orden orden : ordenes) {
            Long usuario = orden.getUsuario().getId();
            ReporteClienteResponse actual = agrupado.get(usuario);
            if (actual == null) {
                agrupado.put(usuario, new ReporteClienteResponse(usuario, orden.getUsuario().getNombre(),
                        1, orden.getTotal()));
            } else {
                agrupado.put(usuario, new ReporteClienteResponse(usuario, actual.nombre(),
                        actual.ordenes() + 1, actual.total().add(orden.getTotal())));
            }
        }
        auditService.registrar(usuarioId, "reporte de clientes generado", "reporte");
        return new ArrayList<>(agrupado.values());
    }

    private List<Orden> ordenesPagadas(String desde, String hasta) {
        LocalDate inicio = parsear(desde, LocalDate.of(1900, 1, 1));
        LocalDate fin = parsear(hasta, LocalDate.of(2100, 12, 31));
        if (inicio.isAfter(fin)) {
            throw ApiException.badRequest("El rango de fechas es inválido");
        }
        Instant desdeInstant = inicio.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant hastaInstant = fin.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusNanos(1);
        return ordenRepository.findByEstadoPagoAndFechaCreacionBetween(
                EstadoPago.pagado, desdeInstant, hastaInstant);
    }

    private LocalDate parsear(String valor, LocalDate porDefecto) {
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException exception) {
            throw ApiException.badRequest("Fechas inválidas; use yyyy-MM-dd");
        }
    }

}
