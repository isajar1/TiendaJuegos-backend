package com.tiendajuegos.cupon;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tiendajuegos.MvcTestSupport;
import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUserService;
import com.tiendajuegos.cupon.CuponDtos.CuponResponse;
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
class CuponControllerTest {

    @Mock private CuponService service;
    @Mock private CurrentUserService currentUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MvcTestSupport.mvc(new CuponController(service, currentUserService));
    }

    private CuponResponse respuesta() {
        return new CuponResponse(1L, "VERANO10", new BigDecimal("10.00"), "2030-12-31T23:59:59", true);
    }

    @Test
    void listarCupones() throws Exception {
        when(service.listar()).thenReturn(List.of(respuesta()));
        mockMvc.perform(get("/api/cupones").principal(TestFixtures.auth("admin@mail.com", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("VERANO10"));
    }

    @Test
    void crearCuponDevuelve201() throws Exception {
        when(currentUserService.id(any())).thenReturn(9L);
        when(service.crear(any(), eq(9L))).thenReturn(respuesta());
        mockMvc.perform(post("/api/cupones")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"VERANO10\",\"porcentajeDescuento\":10,"
                                + "\"fechaExpiracion\":\"2030-12-31T23:59:59\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void crearCuponConDescuentoInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/cupones")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"MALO\",\"porcentajeDescuento\":150,"
                                + "\"fechaExpiracion\":\"2030-12-31T23:59:59\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarActivoDevuelve200() throws Exception {
        when(currentUserService.id(any())).thenReturn(9L);
        when(service.actualizar(eq(1L), any(), eq(9L))).thenReturn(
                new CuponResponse(1L, "VERANO10", new BigDecimal("10"), "2030-12-31T23:59:59", false));
        mockMvc.perform(put("/api/cupones/1")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void cuponInexistenteDevuelve404() throws Exception {
        when(service.listar()).thenThrow(ApiException.notFound("Cupón no encontrado"));
        mockMvc.perform(get("/api/cupones").principal(TestFixtures.auth("admin@mail.com", "ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cupón no encontrado"));
    }
}
