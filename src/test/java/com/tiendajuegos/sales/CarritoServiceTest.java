package com.tiendajuegos.sales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tiendajuegos.catalog.EstadoJuego;
import com.tiendajuegos.catalog.Juego;
import com.tiendajuegos.catalog.JuegoRepository;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.dto.ApiDtos.CarritoRequest;
import com.tiendajuegos.dto.ApiDtos.CarritoResponse;
import com.tiendajuegos.dto.ApiDtos.ItemCarritoRequest;
import com.tiendajuegos.inventory.ClaveDigitalRepository;
import com.tiendajuegos.inventory.EstadoClave;
import com.tiendajuegos.promotions.CuponService;
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

    @Mock
    private JuegoRepository juegoRepository;

    @Mock
    private ClaveDigitalRepository claveRepository;

    @Mock
    private CuponService cuponService;

    private CarritoService service;

    @BeforeEach
    void setUp() {
        service = new CarritoService(juegoRepository, claveRepository, cuponService);
    }

    @Test
    void carritoVacioSeRechaza() {
        assertThrows(ApiException.class, () -> service.calcular(new CarritoRequest(List.of(), null)));
    }

    @Test
    void calculaSubtotalConJuegoDisponible() {
        Juego juego = mock(Juego.class);
        when(juego.getId()).thenReturn(1L);
        when(juego.getTitulo()).thenReturn("FIFA");
        when(juego.getPrecio()).thenReturn(new BigDecimal("50.00"));
        when(juego.getEstado()).thenReturn(EstadoJuego.activo);
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(claveRepository.countByJuegoIdAndEstado(1L, EstadoClave.disponible)).thenReturn(1L);

        CarritoResponse response = service.calcular(
                new CarritoRequest(List.of(new ItemCarritoRequest(1L)), null));

        assertEquals(new BigDecimal("50.00"), response.subtotal());
        assertEquals(new BigDecimal("50.00"), response.total());
        assertEquals(1, response.items().size());
    }
}
