package com.tiendajuegos.sales;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.catalog.Juego;
import com.tiendajuegos.catalog.JuegoRepository;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUser;
import com.tiendajuegos.dto.ApiDtos.*;
import com.tiendajuegos.inventory.ClaveDigital;
import com.tiendajuegos.inventory.ClaveDigitalRepository;
import com.tiendajuegos.inventory.EstadoClave;
import com.tiendajuegos.inventory.KeyCipherService;
import com.tiendajuegos.promotions.Cupon;
import com.tiendajuegos.promotions.CuponRepository;
import com.tiendajuegos.user.Usuario;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

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

    @InjectMocks private CheckoutService checkoutService;

    @Test
    void pagar_requestNulo_lanzaBadRequest() {
        assertThrows(ApiException.class, () -> checkoutService.pagar(null));
    }

    @Test
    void pagar_aprobadoConCupon_entregaClaveYRegistraAuditoria() {
        Usuario u = mock(Usuario.class);
        when(currentUser.usuario()).thenReturn(u);

        CarritoItemResponse item = new CarritoItemResponse(1L, "Halo", new BigDecimal("50.00"), true);
        CarritoResponse carrito = new CarritoResponse(List.of(item), new BigDecimal("50.00"), new BigDecimal("5.00"), new BigDecimal("45.00"), "DESC10");
        when(carritoService.calcular(any())).thenReturn(carrito);
        when(cuponRepository.findByCodigoIgnoreCase("DESC10")).thenReturn(Optional.of(mock(Cupon.class)));
        when(ordenRepository.save(any(Orden.class))).thenAnswer(inv -> inv.getArgument(0));

        ClaveDigital clave = spy(new ClaveDigital(mock(Juego.class), "ENC-KEY"));
        when(claveRepository.findFirstByJuegoIdAndEstado(1L, EstadoClave.disponible)).thenReturn(Optional.of(clave));
        when(detalleRepository.findByOrdenId(any())).thenReturn(List.of());

        OrdenResponse resp = checkoutService.pagar(new CheckoutRequest(List.of(new ItemCarritoRequest(1L)), "DESC10", true));

        assertEquals("pagado", resp.estadoPago());
        verify(clave).vender(any());
        verify(auditService).registrar("PAGO_ORDEN", "ordenes");
        verify(auditService).registrar("ENTREGA_CLAVE", "claves_digitales");
    }

    @Test
    void pagar_sinStockDeClaves_lanzaConflict() {
        when(currentUser.usuario()).thenReturn(mock(Usuario.class));
        CarritoItemResponse item = new CarritoItemResponse(1L, "Halo", new BigDecimal("50.00"), true);
        CarritoResponse carrito = new CarritoResponse(List.of(item), new BigDecimal("50.00"), BigDecimal.ZERO, new BigDecimal("50.00"), null);
        when(carritoService.calcular(any())).thenReturn(carrito);
        when(ordenRepository.save(any(Orden.class))).thenAnswer(inv -> inv.getArgument(0));
        when(claveRepository.findFirstByJuegoIdAndEstado(1L, EstadoClave.disponible)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> checkoutService.pagar(new CheckoutRequest(List.of(new ItemCarritoRequest(1L)), null, true)));
        assertEquals(HttpStatus.CONFLICT, ex.status());
    }

    @Test
    void pagar_rechazado_guardaOrdenRechazadaSinDescontarClaves() {
        when(currentUser.usuario()).thenReturn(mock(Usuario.class));
        CarritoResponse carrito = new CarritoResponse(List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null);
        when(carritoService.calcular(any())).thenReturn(carrito);
        when(ordenRepository.save(any(Orden.class))).thenAnswer(inv -> inv.getArgument(0));
        when(detalleRepository.findByOrdenId(any())).thenReturn(List.of());

        OrdenResponse resp = checkoutService.pagar(new CheckoutRequest(List.of(), null, false));

        assertEquals("rechazado", resp.estadoPago());
        verify(claveRepository, never()).save(any());
        verify(auditService).registrar("PAGO_RECHAZADO", "ordenes");
    }
}