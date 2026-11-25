package com.tiendajuegos.sales;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.tiendajuegos.common.ApiException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private OrdenRepository ordenRepository;
    @InjectMocks private ReportService reportService;

    @Test
    void ventasYCompras_rangoValido_mapeaResultados() {
        LocalDateTime desde = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 12, 31, 23, 59);
        Object[] fila = new Object[]{"Elden Ring", 4L, new BigDecimal("239.96")};
        List<Object[]> filas = java.util.Collections.singletonList(fila);

        when(ordenRepository.ventasPorJuego(any(), eq(desde), eq(hasta))).thenReturn(filas);
        when(ordenRepository.comprasPorCliente(any(), eq(desde), eq(hasta))).thenReturn(filas);

        var ventas = reportService.ventasPorJuego(desde, hasta);
        assertEquals(1, ventas.size());
        assertEquals("Elden Ring", ventas.get(0).etiqueta());
        assertEquals(4L, ventas.get(0).cantidad());

        var compras = reportService.comprasPorCliente(desde, hasta);
        assertEquals(1, compras.size());
    }

    @Test
    void reportes_fechasInvalidas_lanzaBadRequest() {
        LocalDateTime hoy = LocalDateTime.now();
        LocalDateTime ayer = hoy.minusDays(1);

        assertThrows(ApiException.class, () -> reportService.ventasPorJuego(null, hoy));
        assertThrows(ApiException.class, () -> reportService.ventasPorJuego(hoy, null));
        assertThrows(ApiException.class, () -> reportService.comprasPorCliente(hoy, ayer));
    }
}