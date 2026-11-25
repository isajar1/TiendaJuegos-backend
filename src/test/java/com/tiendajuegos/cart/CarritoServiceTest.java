package com.tiendajuegos.cart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.audit.AuditService;
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
import com.tiendajuegos.user.Role;
import com.tiendajuegos.user.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock private CarritoRepository carritoRepository;
    @Mock private CarritoItemRepository itemRepository;
    @Mock private JuegoRepository juegoRepository;
    @Mock private CuponService cuponService;
    @Mock private ClaveDigitalRepository claveRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AuditService auditService;

    private CarritoService service;
    private Juego juego;
    private Carrito carrito;

    @BeforeEach
    void setUp() {
        service = new CarritoService(carritoRepository, itemRepository, juegoRepository, cuponService,
                claveRepository, usuarioRepository, auditService);
        juego = TestFixtures.juego(1L, "Juego", new BigDecimal("100.00"));
        carrito = TestFixtures.carrito(10L, TestFixtures.usuario(5L, "Cliente", "c@mail.com", Role.cliente), null);
    }

    @Test
    void obtenerSinCarritoDevuelveTotalesEnCero() {
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.empty());

        CarritoResponse respuesta = service.obtener(5L);

        assertEquals(0, respuesta.items().size());
        assertEquals(new BigDecimal("0.00"), respuesta.total());
        assertNull(respuesta.cupon());
    }

    @Test
    void agregarCreaCarritoYCalculaSubtotal() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.empty());
        when(usuarioRepository.getReferenceById(5L)).thenReturn(carrito.getUsuario());
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);
        when(itemRepository.findByCarritoIdAndJuegoId(10L, 1L)).thenReturn(Optional.empty());
        when(claveRepository.countByJuegoIdAndEstado(1L, EstadoClave.disponible)).thenReturn(1L);
        when(itemRepository.findByCarritoIdOrderByIdAsc(10L))
                .thenReturn(List.of(TestFixtures.item(1L, carrito, juego)));

        CarritoResponse respuesta = service.agregar(5L, 1L);

        assertEquals(1, respuesta.items().size());
        assertEquals(new BigDecimal("100.00"), respuesta.subtotal());
        assertEquals(new BigDecimal("0.00"), respuesta.descuento());
        assertEquals(new BigDecimal("100.00"), respuesta.total());
        verify(itemRepository).save(any(CarritoItem.class));
        verify(auditService).registrar(5L, "juego agregado al carrito", "carrito");
    }

    @Test
    void agregarDuplicadoNoGuardaOtroItem() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdAndJuegoId(10L, 1L))
                .thenReturn(Optional.of(TestFixtures.item(1L, carrito, juego)));
        when(itemRepository.findByCarritoIdOrderByIdAsc(10L))
                .thenReturn(List.of(TestFixtures.item(1L, carrito, juego)));

        service.agregar(5L, 1L);

        verify(itemRepository, never()).save(any());
        verify(claveRepository, never()).countByJuegoIdAndEstado(any(), any());
    }

    @Test
    void agregarSinStockLanza409() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdAndJuegoId(10L, 1L)).thenReturn(Optional.empty());
        when(claveRepository.countByJuegoIdAndEstado(1L, EstadoClave.disponible)).thenReturn(0L);

        ApiException exception = assertThrows(ApiException.class, () -> service.agregar(5L, 1L));

        assertEquals(409, exception.getStatus().value());
        assertEquals("No hay claves disponibles para Juego", exception.getMessage());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void agregarJuegoInactivoLanza400() {
        Juego inactivo = TestFixtures.inactivo(1L, "Inactivo", BigDecimal.TEN);
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(inactivo));
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdAndJuegoId(10L, 1L)).thenReturn(Optional.empty());

        assertEquals(400, assertThrows(ApiException.class, () -> service.agregar(5L, 1L))
                .getStatus().value());
    }

    @Test
    void agregarJuegoInexistenteLanza404() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ApiException.class, () -> service.agregar(5L, 1L))
                .getStatus().value());
    }

    @Test
    void eliminarItemQuitaDelCarrito() {
        CarritoItem item = TestFixtures.item(1L, carrito, juego);
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdAndJuegoId(10L, 1L)).thenReturn(Optional.of(item));
        when(itemRepository.findByCarritoIdOrderByIdAsc(10L)).thenReturn(List.of());

        CarritoResponse respuesta = service.eliminarItem(5L, 1L);

        assertEquals(0, respuesta.items().size());
        verify(itemRepository).delete(item);
        verify(auditService).registrar(5L, "juego eliminado del carrito", "carrito");
    }

    @Test
    void eliminarItemInexistenteNoRegistraAuditoria() {
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdAndJuegoId(10L, 1L)).thenReturn(Optional.empty());
        when(itemRepository.findByCarritoIdOrderByIdAsc(10L)).thenReturn(List.of());

        service.eliminarItem(5L, 1L);

        verify(itemRepository, never()).delete(any());
        verify(auditService, never()).registrar(any(), any(), any());
    }

    @Test
    void vaciarEliminaItemsYQuitaCupon() {
        carrito.setCupon(TestFixtures.cupon(2L, "CUPON", new BigDecimal("10"), true));
        List<CarritoItem> items = List.of(TestFixtures.item(1L, carrito, juego));
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdOrderByIdAsc(10L)).thenReturn(items);

        service.vaciar(5L);

        verify(itemRepository).deleteAll(items);
        assertNull(carrito.getCupon());
        verify(carritoRepository).save(carrito);
    }

    @Test
    void aplicarCuponSinCarritoLanza400() {
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.empty());
        assertEquals(400, assertThrows(ApiException.class,
                () -> service.aplicarCupon(5L, new CodigoRequest("CUPON"))).getStatus().value());
    }

    @Test
    void aplicarCuponACarritoVacioLanza400() {
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdOrderByIdAsc(10L)).thenReturn(List.of());
        assertEquals(400, assertThrows(ApiException.class,
                () -> service.aplicarCupon(5L, new CodigoRequest("CUPON"))).getStatus().value());
    }

    @Test
    void aplicarCuponValidoCalculaDescuento() {
        Cupon cupon = TestFixtures.cupon(2L, "CUPON", new BigDecimal("10.00"), true);
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdOrderByIdAsc(10L))
                .thenReturn(List.of(TestFixtures.item(1L, carrito, juego)));
        when(cuponService.validar("cupon")).thenReturn(cupon);
        when(cuponService.porcentaje(cupon)).thenReturn(new BigDecimal("10.00"));
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        CarritoResponse respuesta = service.aplicarCupon(5L, new CodigoRequest("cupon"));

        assertEquals("CUPON", respuesta.cupon());
        assertEquals(new BigDecimal("10.00"), respuesta.descuento());
        assertEquals(new BigDecimal("90.00"), respuesta.total());
        verify(auditService).registrar(5L, "cupón aplicado al carrito", "carrito");
    }

    @Test
    void aplicarCuponInvalidoPropagaError() {
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdOrderByIdAsc(10L))
                .thenReturn(List.of(TestFixtures.item(1L, carrito, juego)));
        when(cuponService.validar("MALO")).thenThrow(ApiException.badRequest("El cupón no está vigente"));

        ApiException exception = assertThrows(ApiException.class,
                () -> service.aplicarCupon(5L, new CodigoRequest("MALO")));

        assertEquals("El cupón no está vigente", exception.getMessage());
        verify(carritoRepository, never()).save(any());
    }
}
