package com.tiendajuegos.reportes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tiendajuegos.MvcTestSupport;
import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUserService;
import com.tiendajuegos.reportes.ReportesDtos.ReporteClienteResponse;
import com.tiendajuegos.reportes.ReportesDtos.ReporteVentaResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
class ReporteControllerTest {

    @Mock private ReporteService service;
    @Mock private CurrentUserService currentUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MvcTestSupport.mvc(new ReporteController(service, currentUserService));
        org.mockito.Mockito.lenient().when(currentUserService.id(any())).thenReturn(9L);
    }

    @Test
    void reporteVentas() throws Exception {
        when(service.ventasPorJuego("2026-01-01", "2026-12-31", 9L))
                .thenReturn(List.of(new ReporteVentaResponse(1L, "Juego", 5, new BigDecimal("250.00"))));
        mockMvc.perform(get("/api/reportes/ventas-juegos")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .param("desde", "2026-01-01").param("hasta", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unidades").value(5));
    }

    @Test
    void reporteClientes() throws Exception {
        when(service.comprasPorCliente(eq(null), eq(null), eq(9L)))
                .thenReturn(List.of(new ReporteClienteResponse(5L, "Cliente", 3, new BigDecimal("300.00"))));
        mockMvc.perform(get("/api/reportes/compras-clientes")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Cliente"));
    }

    @Test
    void rangoInvalidoDevuelve400() throws Exception {
        when(service.ventasPorJuego("2026-12-31", "2026-01-01", 9L))
                .thenThrow(ApiException.badRequest("El rango de fechas es inválido"));
        mockMvc.perform(get("/api/reportes/ventas-juegos")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .param("desde", "2026-12-31").param("hasta", "2026-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El rango de fechas es inválido"));
    }
}
