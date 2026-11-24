package com.tiendajuegos.sales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.catalog.Juego;
import com.tiendajuegos.catalog.JuegoRepository;
import com.tiendajuegos.common.CurrentUser;
import com.tiendajuegos.dto.ApiDtos.CarritoItemResponse;
import com.tiendajuegos.dto.ApiDtos.CarritoResponse;
import com.tiendajuegos.dto.ApiDtos.CheckoutRequest;
import com.tiendajuegos.dto.ApiDtos.ItemCarritoRequest;
import com.tiendajuegos.dto.ApiDtos.OrdenResponse;
import com.tiendajuegos.inventory.ClaveDigital;
import com.tiendajuegos.inventory.ClaveDigitalRepository;
import com.tiendajuegos.inventory.EstadoClave;
import com.tiendajuegos.inventory.KeyCipherService;
import com.tiendajuegos.promotions.CuponRepository;
import com.tiendajuegos.user.Usuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock private CarritoService carritoService;
    @Mock private OrdenRepository ordenRepository;
    @Mock private DetalleOrdenRepository detalleRepository;
    @Mock private JuegoRepository juegoRepository;
    @Mock private ClaveDigitalRepository claveRepository;
    @Mock private CuponRepository cuponRepository;
    @Mock private KeyCipherService cipher;
    @Mock private CurrentUser currentUser;
    @Mock private AuditService auditService;
    @Mock private Orden orden;
    @Mock private ClaveDigital clave;
    @Mock private DetalleOrden detalle;
    @Mock private Juego juego;
    @Mock private Usuario usuario;

    private CheckoutService service;

    @BeforeEach
    void setUp() {
        service = new CheckoutService(carritoService, ordenRepository, detalleRepository,
                juegoRepository, claveRepository, cuponRepository, cipher, currentUser, auditService);
    }

    @Test
    void pagoAprobadoAsignaClaveYLaEntregaAlCliente() {
        BigDecimal precio = new BigDecimal("50.00");
        CarritoResponse carrito = new CarritoResponse(
                List.of(new CarritoItemResponse(1L, "FIFA", precio, true)),
                precio, BigDecimal.ZERO, precio, null);
        when(carritoService.calcular(any())).thenReturn(carrito);
        when(currentUser.usuario()).thenReturn(usuario);
        when(ordenRepository.save(any(Orden.class))).thenReturn(orden);
        when(orden.getId()).thenReturn(10L);
        when(orden.getEstadoPago()).thenReturn(EstadoPago.pagado);
        when(orden.getTotal()).thenReturn(precio);
        when(orden.getFechaCreacion()).thenReturn(LocalDateTime.now());
        when(claveRepository.findFirstByJuegoIdAndEstado(1L, EstadoClave.disponible))
                .thenReturn(Optional.of(clave));
        when(clave.getId()).thenReturn(5L);
        when(clave.getClaveCifrada()).thenReturn("CLAVE-CIFRADA");
        when(juegoRepository.getReferenceById(1L)).thenReturn(juego);
        when(detalleRepository.findByOrdenId(10L)).thenReturn(List.of(detalle));
        when(detalle.getJuego()).thenReturn(juego);
        when(juego.getId()).thenReturn(1L);
        when(juego.getTitulo()).thenReturn("FIFA");
        when(detalle.getPrecioUnitario()).thenReturn(precio);
        when(detalle.getClaveId()).thenReturn(5L);
        when(claveRepository.findById(5L)).thenReturn(Optional.of(clave));
        when(cipher.descifrar("CLAVE-CIFRADA")).thenReturn("ABCDE-12345");

        OrdenResponse response = service.pagar(
                new CheckoutRequest(List.of(new ItemCarritoRequest(1L)), null, true));

        assertEquals(EstadoPago.pagado.name(), response.estadoPago());
        assertEquals("ABCDE-12345", response.detalles().get(0).clave());
        verify(clave).vender(10L);
    }
}
