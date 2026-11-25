package com.tiendajuegos.reportes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.domain.DetalleOrden;
import com.tiendajuegos.domain.EstadoPago;
import com.tiendajuegos.domain.Orden;
import com.tiendajuegos.repository.DetalleOrdenRepository;
import com.tiendajuegos.repository.OrdenRepository;
import com.tiendajuegos.user.Role;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock private OrdenRepository ordenRepository;
    @Mock private DetalleOrdenRepository detalleRepository;
    @Mock private AuditService auditService;

    private ReporteService service;
    private Orden ordenUno;
    private Orden ordenDos;

    @BeforeEach
    void setUp() {
        service = new ReporteService(ordenRepository, detalleRepository, auditService);
        var cliente = TestFixtures.usuario(5L, "Cliente", "c@mail.com", Role.cliente);
        ordenUno = TestFixtures.orden(1L, cliente, null, new BigDecimal("100.00"), EstadoPago.pagado);
        ordenDos = TestFixtures.orden(2L, cliente, null, new BigDecimal("50.00"), EstadoPago.pagado);
        org.springframework.test.util.ReflectionTestUtils.setField(ordenDos, "fechaCreacion",
                Instant.parse("2026-02-15T10:00:00Z"));
    }

    private void stubOrdenes(List<Orden> ordenes) {
        when(ordenRepository.findByEstadoPagoAndFechaCreacionBetween(
                eq(EstadoPago.pagado), any(Instant.class), any(Instant.class))).thenReturn(ordenes);
    }

    @Test
    void ventasAgrupaPorJuegoYSoloOrdenesPagadas() {
        var juego = TestFixtures.juego(1L, "Juego", new BigDecimal("50.00"));
        DetalleOrden detalleUno = TestFixtures.detalle(1L, ordenUno, juego, new BigDecimal("50.00"), null);
        DetalleOrden detalleDos = TestFixtures.detalle(2L, ordenDos, juego, new BigDecimal("50.00"), null);
        stubOrdenes(List.of(ordenUno, ordenDos));
        when(detalleRepository.findByOrdenIdOrderByIdAsc(1L)).thenReturn(List.of(detalleUno));
        when(detalleRepository.findByOrdenIdOrderByIdAsc(2L)).thenReturn(List.of(detalleDos));

        var respuesta = service.ventasPorJuego("2026-01-01", "2026-12-31", 9L);

        assertEquals(1, respuesta.size());
        assertEquals("Juego", respuesta.get(0).titulo());
        assertEquals(2L, respuesta.get(0).unidades());
        assertEquals(new BigDecimal("100.00"), respuesta.get(0).ingresos());
        verify(auditService).registrar(9L, "reporte de ventas generado", "reporte");
    }

    @Test
    void ventasSinDatosDevuelveListaVacia() {
        stubOrdenes(List.of());
        assertEquals(0, service.ventasPorJuego(null, null, 9L).size());
    }

    @Test
    void ventasConRangoInvertidoLanza400() {
        ApiException exception = assertThrows(ApiException.class,
                () -> service.ventasPorJuego("2026-12-31", "2026-01-01", 9L));
        assertEquals(400, exception.getStatus().value());
        assertEquals("El rango de fechas es inválido", exception.getMessage());
    }

    @Test
    void ventasConFechaInvalidaLanza400() {
        assertEquals(400, assertThrows(ApiException.class,
                () -> service.ventasPorJuego("fecha-mala", null, 9L)).getStatus().value());
    }

    @Test
    void comprasPorClienteAgrupaOrdenes() {
        stubOrdenes(List.of(ordenUno, ordenDos));

        var respuesta = service.comprasPorCliente("2026-01-01", "2026-12-31", 9L);

        assertEquals(1, respuesta.size());
        assertEquals("Cliente", respuesta.get(0).nombre());
        assertEquals(2L, respuesta.get(0).ordenes());
        assertEquals(new BigDecimal("150.00"), respuesta.get(0).total());
        verify(auditService).registrar(9L, "reporte de clientes generado", "reporte");
    }

    @Test
    void comprasSinDatosDevuelveListaVacia() {
        stubOrdenes(List.of());
        assertEquals(0, service.comprasPorCliente(null, null, 9L).size());
    }

    @Test
    void comprasConRangoInvertidoLanza400() {
        assertEquals(400, assertThrows(ApiException.class,
                () -> service.comprasPorCliente("2026-02-01", "2026-01-01", 9L)).getStatus().value());
    }
}
