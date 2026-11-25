package com.tiendajuegos.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.crypto.CryptoService;
import com.tiendajuegos.domain.DetalleOrden;
import com.tiendajuegos.domain.EstadoPago;
import com.tiendajuegos.domain.Orden;
import com.tiendajuegos.orders.OrderDtos.EstadoRequest;
import com.tiendajuegos.repository.DetalleOrdenRepository;
import com.tiendajuegos.repository.OrdenRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock private OrdenRepository ordenRepository;
    @Mock private DetalleOrdenRepository detalleRepository;
    @Mock private CryptoService cryptoService;
    @Mock private AuditService auditService;

    private OrdenService service;
    private Orden orden;

    @BeforeEach
    void setUp() {
        service = new OrdenService(ordenRepository, detalleRepository, cryptoService, auditService);
        orden = TestFixtures.orden(20L, TestFixtures.usuario(5L, "Cliente", "c@mail.com", null),
                TestFixtures.cupon(3L, "CUPON", new BigDecimal("10"), true),
                new BigDecimal("90.00"), EstadoPago.pagado);
    }

    @Test
    void miasMapeaDetallesConClavesDescifradas() {
        DetalleOrden detalle = TestFixtures.detalle(1L, orden, TestFixtures.juego(1L, "Juego", BigDecimal.TEN),
                BigDecimal.TEN, TestFixtures.clave(2L, null, "cifrada"));
        when(ordenRepository.findByUsuarioIdOrderByFechaCreacionDesc(5L)).thenReturn(List.of(orden));
        when(detalleRepository.findByOrdenIdOrderByIdAsc(20L)).thenReturn(List.of(detalle));
        when(cryptoService.decrypt("cifrada")).thenReturn("CLAVE-LIMPIA");

        var respuesta = service.mias(5L);

        assertEquals(1, respuesta.size());
        assertEquals("CLAVE-LIMPIA", respuesta.get(0).items().get(0).clave());
        assertEquals("CUPON", respuesta.get(0).cupon());
        assertEquals("pagado", respuesta.get(0).estadoPago());
    }

    @Test
    void todasDevuelveLasOrdenes() {
        when(ordenRepository.findAllByOrderByFechaCreacionDesc()).thenReturn(List.of(orden));
        when(detalleRepository.findByOrdenIdOrderByIdAsc(20L)).thenReturn(List.of());

        assertEquals(1, service.todas().size());
    }

    @Test
    void obtenerMiaDeOtroUsuarioLanza404() {
        when(ordenRepository.findById(20L)).thenReturn(Optional.of(orden));
        assertEquals(404, assertThrows(ApiException.class,
                () -> service.obtenerMia(6L, 20L)).getStatus().value());
    }

    @Test
    void obtenerMiaPropietarioDevuelveOrden() {
        when(ordenRepository.findById(20L)).thenReturn(Optional.of(orden));
        when(detalleRepository.findByOrdenIdOrderByIdAsc(20L)).thenReturn(List.of());
        assertEquals(20L, service.obtenerMia(5L, 20L).id());
    }

    @Test
    void obtenerInexistenteLanza404() {
        when(ordenRepository.findById(20L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ApiException.class, () -> service.obtener(20L)).getStatus().value());
    }

    @Test
    void cambiarEstadoActualizaYAudita() {
        when(ordenRepository.findById(20L)).thenReturn(Optional.of(orden));
        when(ordenRepository.save(orden)).thenReturn(orden);
        when(detalleRepository.findByOrdenIdOrderByIdAsc(20L)).thenReturn(List.of());

        var respuesta = service.cambiarEstado(20L, new EstadoRequest("Reembolsado"), 7L);

        assertEquals("reembolsado", respuesta.estadoPago());
        verify(auditService).registrar(7L, "estado de orden actualizado", "orden");
    }

    @Test
    void cambiarEstadoConValorInvalidoLanza400() {
        when(ordenRepository.findById(20L)).thenReturn(Optional.of(orden));
        assertEquals(400, assertThrows(ApiException.class,
                () -> service.cambiarEstado(20L, new EstadoRequest("no-valido"), 7L)).getStatus().value());
    }

    @Test
    void obtenerSinDetallesDevuelveItemsVacios() {
        when(ordenRepository.findById(20L)).thenReturn(Optional.of(orden));
        when(detalleRepository.findByOrdenIdOrderByIdAsc(20L)).thenReturn(List.of());
        assertEquals(0, service.obtener(20L).items().size());
    }
}
