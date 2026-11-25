package com.tiendajuegos.orders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tiendajuegos.MvcTestSupport;
import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUserService;
import com.tiendajuegos.orders.OrderDtos.EstadoRequest;
import com.tiendajuegos.orders.OrderDtos.OrdenResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
class OrdenControllerTest {

    @Mock private OrdenService service;
    @Mock private CurrentUserService currentUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MvcTestSupport.mvc(new OrdenController(service, currentUserService));
        org.mockito.Mockito.lenient().when(currentUserService.id(any())).thenReturn(5L);
    }

    private OrdenResponse respuesta() {
        return new OrdenResponse(20L, "2026-01-15T10:00:00Z", new BigDecimal("90.00"), "pagado", null, List.of());
    }

    @Test
    void ordenesMias() throws Exception {
        when(service.mias(5L)).thenReturn(List.of(respuesta()));
        mockMvc.perform(get("/api/ordenes/mias").principal(TestFixtures.auth("cliente@mail.com", "CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20));
    }

    @Test
    void ordenMiaPorId() throws Exception {
        when(service.obtenerMia(5L, 20L)).thenReturn(respuesta());
        mockMvc.perform(get("/api/ordenes/mias/20").principal(TestFixtures.auth("cliente@mail.com", "CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(90.00));
    }

    @Test
    void ordenesTodasParaAdmin() throws Exception {
        when(service.todas()).thenReturn(List.of(respuesta()));
        mockMvc.perform(get("/api/ordenes").principal(TestFixtures.auth("admin@mail.com", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estadoPago").value("pagado"));
    }

    @Test
    void ordenPorIdParaAdmin() throws Exception {
        when(service.obtener(20L)).thenReturn(respuesta());
        mockMvc.perform(get("/api/ordenes/20").principal(TestFixtures.auth("admin@mail.com", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarEstado() throws Exception {
        when(service.cambiarEstado(eq(20L), any(EstadoRequest.class), eq(5L))).thenReturn(respuesta());
        mockMvc.perform(put("/api/ordenes/20/estado")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estadoPago\":\"reembolsado\"}"))
                .andExpect(status().isOk());
        verify(service).cambiarEstado(eq(20L), any(EstadoRequest.class), eq(5L));
    }

    @Test
    void ordenInexistenteDevuelve404() throws Exception {
        when(service.obtener(99L)).thenThrow(ApiException.notFound("Orden no encontrada"));
        mockMvc.perform(get("/api/ordenes/99").principal(TestFixtures.auth("admin@mail.com", "ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Orden no encontrada"));
    }
}
