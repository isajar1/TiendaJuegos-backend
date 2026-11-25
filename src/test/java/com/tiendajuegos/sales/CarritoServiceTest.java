package com.tiendajuegos.sales;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tiendajuegos.catalog.EstadoJuego;
import com.tiendajuegos.catalog.Juego;
import com.tiendajuegos.catalog.JuegoRepository;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.dto.ApiDtos.CarritoRequest;
import com.tiendajuegos.dto.ApiDtos.ItemCarritoRequest;
import com.tiendajuegos.inventory.ClaveDigitalRepository;
import com.tiendajuegos.inventory.EstadoClave;
import com.tiendajuegos.promotions.Cupon;
import com.tiendajuegos.promotions.CuponService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock private JuegoRepository juegoRepository;
    @Mock private ClaveDigitalRepository claveRepository;
    @Mock private CuponService cuponService;

    @InjectMocks private CarritoService carritoService;

    @Test
    void calcular_conCuponYStock_calculaSubtotalDescuentoYTotal() {
        Juego j = mock(Juego.class);
        when(j.getId()).thenReturn(1L);
        when(j.getTitulo()).thenReturn("FIFA");
        when(j.getPrecio()).thenReturn(new BigDecimal("60.00"));
        when(j.getEstado()).thenReturn(EstadoJuego.activo);

        when(juegoRepository.findById(1L)).thenReturn(Optional.of(j));
        when(claveRepository.countByJuegoIdAndEstado(1L, EstadoClave.disponible)).thenReturn(2L);

        Cupon cupon = new Cupon("DESC10", BigDecimal.TEN, LocalDateTime.now().plusDays(1), true);
        when(cuponService.validar("DESC10")).thenReturn(cupon);
        when(cuponService.descuento(new BigDecimal("60.00"), cupon)).thenReturn(new BigDecimal("6.00"));

        var resp = carritoService.calcular(new CarritoRequest(List.of(new ItemCarritoRequest(1L)), "DESC10"));

        assertEquals(new BigDecimal("60.00"), resp.subtotal());
        assertEquals(new BigDecimal("6.00"), resp.descuento());
        assertEquals(new BigDecimal("54.00"), resp.total());
        assertEquals("DESC10", resp.cupon());
    }

    @Test
    void calcular_erroresDeValidacionYStock() {
        assertThrows(ApiException.class, () -> carritoService.calcular(null));
        assertThrows(ApiException.class, () -> carritoService.calcular(new CarritoRequest(List.of(), null)));
        assertThrows(ApiException.class, () -> carritoService.calcular(
                new CarritoRequest(List.of(new ItemCarritoRequest(null)), null)));
        assertThrows(ApiException.class, () -> carritoService.calcular(
                new CarritoRequest(List.of(new ItemCarritoRequest(1L), new ItemCarritoRequest(1L)), null)));

        when(juegoRepository.findById(99L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ApiException.class,
                () -> carritoService.calcular(new CarritoRequest(List.of(new ItemCarritoRequest(99L)), null))).status());

        Juego inactivo = mock(Juego.class);
        when(inactivo.getEstado()).thenReturn(EstadoJuego.inactivo);
        when(inactivo.getTitulo()).thenReturn("Viejo");
        when(juegoRepository.findById(2L)).thenReturn(Optional.of(inactivo));
        assertThrows(ApiException.class,
                () -> carritoService.calcular(new CarritoRequest(List.of(new ItemCarritoRequest(2L)), null)));

        Juego sinStock = mock(Juego.class);
        when(sinStock.getId()).thenReturn(3L);
        when(sinStock.getEstado()).thenReturn(EstadoJuego.activo);
        when(sinStock.getTitulo()).thenReturn("Agotado");
        when(juegoRepository.findById(3L)).thenReturn(Optional.of(sinStock));
        when(claveRepository.countByJuegoIdAndEstado(3L, EstadoClave.disponible)).thenReturn(0L);
        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class,
                () -> carritoService.calcular(new CarritoRequest(List.of(new ItemCarritoRequest(3L)), null))).status());
    }
}