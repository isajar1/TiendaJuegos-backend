package com.tiendajuegos.orders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tiendajuegos.MvcTestSupport;
import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUserService;
import com.tiendajuegos.orders.OrderDtos.OrdenItemResponse;
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
class CheckoutControllerTest {

    @Mock private CheckoutService service;
    @Mock private CurrentUserService currentUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MvcTestSupport.mvc(new CheckoutController(service, currentUserService));
        org.mockito.Mockito.lenient().when(currentUserService.id(any())).thenReturn(5L);
    }

    private OrdenResponse respuesta(String estado, String clave) {
        return new OrdenResponse(7L, "2026-01-15T10:00:00Z", new BigDecimal("100.00"), estado, null,
                List.of(new OrdenItemResponse(1L, "Juego", new BigDecimal("100.00"), clave)));
    }

    @Test
    void pagarPorDefectoApruebaElPago() throws Exception {
        when(service.pagar(5L, true)).thenReturn(respuesta("pagado", "CLAVE-LIMPIA"));

        mockMvc.perform(post("/api/checkout/pagar")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoPago").value("pagado"))
                .andExpect(jsonPath("$.items[0].clave").value("CLAVE-LIMPIA"));
        verify(service).pagar(5L, true);
    }

    @Test
    void pagarConAprobarFalseConservaElCarrito() throws Exception {
        when(service.pagar(5L, false)).thenReturn(respuesta("rechazado", ""));

        mockMvc.perform(post("/api/checkout/pagar")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprobar\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoPago").value("rechazado"));
        verify(service).pagar(5L, false);
    }

    @Test
    void pagarCarritoVacioDevuelve400() throws Exception {
        when(service.pagar(5L, true)).thenThrow(ApiException.badRequest("El carrito está vacío"));
        mockMvc.perform(post("/api/checkout/pagar")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El carrito está vacío"));
    }
}
