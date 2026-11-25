package com.tiendajuegos.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.cart.CarritoService;
import com.tiendajuegos.cart.CartDtos.CarritoItemResponse;
import com.tiendajuegos.cart.CartDtos.CarritoResponse;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.crypto.CryptoService;
import com.tiendajuegos.domain.Carrito;
import com.tiendajuegos.domain.CarritoItem;
import com.tiendajuegos.domain.ClaveDigital;
import com.tiendajuegos.domain.DetalleOrden;
import com.tiendajuegos.domain.EstadoClave;
import com.tiendajuegos.domain.EstadoPago;
import com.tiendajuegos.domain.Juego;
import com.tiendajuegos.repository.CarritoItemRepository;
import com.tiendajuegos.repository.CarritoRepository;
import com.tiendajuegos.repository.ClaveDigitalRepository;
import com.tiendajuegos.repository.DetalleOrdenRepository;
import com.tiendajuegos.repository.OrdenRepository;
import com.tiendajuegos.user.Role;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock private CarritoRepository carritoRepository;
    @Mock private CarritoItemRepository itemRepository;
    @Mock private CarritoService carritoService;
    @Mock private OrdenRepository ordenRepository;
    @Mock private DetalleOrdenRepository detalleRepository;
    @Mock private ClaveDigitalRepository claveRepository;
    @Mock private CryptoService cryptoService;
    @Mock private AuditService auditService;

    private CheckoutService service;
    private Carrito carrito;
    private Juego juegoA;
    private Juego juegoB;
    private List<CarritoItem> items;

    @BeforeEach
    void setUp() {
        service = new CheckoutService(carritoRepository, itemRepository, carritoService, ordenRepository,
                detalleRepository, claveRepository, cryptoService, auditService);
        carrito = TestFixtures.carrito(10L,
                TestFixtures.usuario(5L, "Cliente", "c@mail.com", Role.cliente), null);
        juegoA = TestFixtures.juego(1L, "Juego A", new BigDecimal("100.00"));
        juegoB = TestFixtures.juego(2L, "Juego B", new BigDecimal("50.00"));
        items = List.of(TestFixtures.item(1L, carrito, juegoA), TestFixtures.item(2L, carrito, juegoB));
    }

    private CarritoResponse resumen() {
        return new CarritoResponse(List.of(
                new CarritoItemResponse(1L, "Juego A", new BigDecimal("100.00")),
                new CarritoItemResponse(2L, "Juego B", new BigDecimal("50.00"))),
                new BigDecimal("150.00"), BigDecimal.ZERO, new BigDecimal("150.00"), null);
    }

    @Test
    void pagarSinCarritoLanza400() {
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.empty());
        assertEquals(400, assertThrows(ApiException.class,
                () -> service.pagar(5L, true)).getStatus().value());
    }

    @Test
    void pagarConCarritoVacioLanza400() {
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdOrderByIdAsc(10L)).thenReturn(List.of());
        when(carritoService.resumen(carrito)).thenReturn(resumen());
        assertEquals(400, assertThrows(ApiException.class,
                () -> service.pagar(5L, true)).getStatus().value());
    }

    @Test
    void pagoAprobadoAsignaClavesDescifradasYVaciaCarrito() {
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdOrderByIdAsc(10L)).thenReturn(items);
        when(carritoService.resumen(carrito)).thenReturn(resumen());
        when(claveRepository.countByJuegoIdAndEstado(1L, EstadoClave.disponible)).thenReturn(1L);
        when(claveRepository.countByJuegoIdAndEstado(2L, EstadoClave.disponible)).thenReturn(2L);
        ClaveDigital claveA = TestFixtures.clave(100L, juegoA, "cifrada-A");
        ClaveDigital claveB = TestFixtures.clave(101L, juegoB, "cifrada-B");
        when(claveRepository.findFirstByJuegoIdAndEstadoOrderByIdAsc(1L, EstadoClave.disponible))
                .thenReturn(Optional.of(claveA));
        when(claveRepository.findFirstByJuegoIdAndEstadoOrderByIdAsc(2L, EstadoClave.disponible))
                .thenReturn(Optional.of(claveB));
        when(cryptoService.decrypt("cifrada-A")).thenReturn("CLAVE-A");
        when(cryptoService.decrypt("cifrada-B")).thenReturn("CLAVE-B");
        when(ordenRepository.save(any())).thenReturn(TestFixtures.orden(99L, carrito.getUsuario(), null,
                new BigDecimal("150.00"), EstadoPago.pagado));

        var respuesta = service.pagar(5L, true);

        assertEquals("pagado", respuesta.estadoPago());
        assertEquals(99L, respuesta.id());
        assertEquals(new BigDecimal("150.00"), respuesta.total());
        assertEquals(2, respuesta.items().size());
        assertEquals("CLAVE-A", respuesta.items().get(0).clave());
        assertEquals("CLAVE-B", respuesta.items().get(1).clave());
        assertEquals(EstadoClave.vendida, claveA.getEstado());
        assertEquals(99L, claveA.getOrden().getId());
        ArgumentCaptor<List<DetalleOrden>> captor = ArgumentCaptor.forClass(List.class);
        verify(detalleRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
        verify(carritoService).vaciar(5L);
        verify(auditService).registrar(5L, "pago aprobado", "orden");
        verify(auditService, times(2)).registrar(5L, "clave entregada", "clave_digital");
    }

    @Test
    void pagoAprobadoSinStockLanza409YNoCreaOrden() {
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdOrderByIdAsc(10L)).thenReturn(items);
        when(carritoService.resumen(carrito)).thenReturn(resumen());
        when(claveRepository.countByJuegoIdAndEstado(1L, EstadoClave.disponible)).thenReturn(0L);

        ApiException exception = assertThrows(ApiException.class, () -> service.pagar(5L, true));

        assertEquals(409, exception.getStatus().value());
        assertEquals("No hay claves disponibles para Juego A", exception.getMessage());
        verify(ordenRepository, never()).save(any());
        verify(carritoService, never()).vaciar(any());
    }

    @Test
    void pagoAprobadoSinClaveTrasVerificarLanza409() {
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdOrderByIdAsc(10L)).thenReturn(items);
        when(carritoService.resumen(carrito)).thenReturn(resumen());
        when(claveRepository.countByJuegoIdAndEstado(1L, EstadoClave.disponible)).thenReturn(1L);
        when(claveRepository.countByJuegoIdAndEstado(2L, EstadoClave.disponible)).thenReturn(1L);
        when(ordenRepository.save(any())).thenReturn(TestFixtures.orden(99L, carrito.getUsuario(), null,
                new BigDecimal("150.00"), EstadoPago.pagado));
        when(claveRepository.findFirstByJuegoIdAndEstadoOrderByIdAsc(1L, EstadoClave.disponible))
                .thenReturn(Optional.empty());

        assertEquals(409, assertThrows(ApiException.class, () -> service.pagar(5L, true))
                .getStatus().value());
    }

    @Test
    void pagoRechazadoConservaCarritoYClaves() {
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdOrderByIdAsc(10L)).thenReturn(items);
        when(carritoService.resumen(carrito)).thenReturn(resumen());
        when(ordenRepository.save(any())).thenAnswer(invocation -> {
            var orden = invocation.getArgument(0, com.tiendajuegos.domain.Orden.class);
            org.springframework.test.util.ReflectionTestUtils.setField(orden, "id", 100L);
            return orden;
        });

        var respuesta = service.pagar(5L, false);

        assertEquals("rechazado", respuesta.estadoPago());
        assertEquals(100L, respuesta.id());
        assertEquals(2, respuesta.items().size());
        assertEquals("", respuesta.items().get(0).clave());
        verify(carritoService, never()).vaciar(any());
        verify(claveRepository, never()).findFirstByJuegoIdAndEstadoOrderByIdAsc(any(), any());
        verify(claveRepository, never()).save(any());
        verify(detalleRepository, never()).saveAll(anyList());
        verify(auditService).registrar(5L, "pago rechazado", "orden");
    }
}
