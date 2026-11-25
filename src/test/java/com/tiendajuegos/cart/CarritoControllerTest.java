package com.tiendajuegos.cart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tiendajuegos.MvcTestSupport;
import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.cart.CartDtos.CarritoItemResponse;
import com.tiendajuegos.cart.CartDtos.CarritoResponse;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUserService;
import com.tiendajuegos.cupon.CuponDtos.CodigoRequest;
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
class CarritoControllerTest {

    @Mock private CarritoService service;
    @Mock private CurrentUserService currentUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MvcTestSupport.mvc(new CarritoController(service, currentUserService));
        org.mockito.Mockito.lenient().when(currentUserService.id(any())).thenReturn(5L);
    }

    private CarritoResponse respuesta() {
        return new CarritoResponse(List.of(new CarritoItemResponse(1L, "Juego", new BigDecimal("50.00"))),
                new BigDecimal("50.00"), new BigDecimal("5.00"), new BigDecimal("45.00"), "CUPON");
    }

    @Test
    void obtenerCarrito() throws Exception {
        when(service.obtener(5L)).thenReturn(respuesta());
        mockMvc.perform(get("/api/carrito").principal(TestFixtures.auth("cliente@mail.com", "CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].juegoId").value(1))
                .andExpect(jsonPath("$.total").value(45.00))
                .andExpect(jsonPath("$.cupon").value("CUPON"));
    }

    @Test
    void agregarItemDevuelveCarrito() throws Exception {
        when(service.agregar(5L, 1L)).thenReturn(respuesta());
        mockMvc.perform(post("/api/carrito/items")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"juegoId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(50.00));
    }

    @Test
    void agregarItemSinStockDevuelve409() throws Exception {
        when(service.agregar(5L, 1L))
                .thenThrow(ApiException.conflict("No hay claves disponibles para Juego"));
        mockMvc.perform(post("/api/carrito/items")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"juegoId\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No hay claves disponibles para Juego"));
    }

    @Test
    void eliminarItemDevuelveCarrito() throws Exception {
        when(service.eliminarItem(5L, 1L)).thenReturn(respuesta());
        mockMvc.perform(delete("/api/carrito/items/1")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].titulo").value("Juego"));
    }

    @Test
    void vaciarCarritoDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/carrito").principal(TestFixtures.auth("cliente@mail.com", "CLIENTE")))
                .andExpect(status().isNoContent());
        verify(service).vaciar(5L);
    }

    @Test
    void aplicarCuponDevuelveCarritoConDescuento() throws Exception {
        when(service.aplicarCupon(eq(5L), any(CodigoRequest.class))).thenReturn(respuesta());
        mockMvc.perform(post("/api/carrito/cupon")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"CUPON\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descuento").value(5.00));
    }
}
