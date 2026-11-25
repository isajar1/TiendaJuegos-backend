package com.tiendajuegos.deseos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tiendajuegos.MvcTestSupport;
import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.common.CurrentUserService;
import com.tiendajuegos.deseos.DeseosDtos.DeseoResponse;
import com.tiendajuegos.deseos.DeseosDtos.DeseosResult;
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
class DeseosControllerTest {

    @Mock private DeseosService service;
    @Mock private CurrentUserService currentUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MvcTestSupport.mvc(new DeseosController(service, currentUserService));
        org.mockito.Mockito.lenient().when(currentUserService.id(any())).thenReturn(5L);
    }

    private DeseoResponse respuesta() {
        return new DeseoResponse(1L, "Juego", new BigDecimal("49.90"));
    }

    @Test
    void listarDeseos() throws Exception {
        when(service.listar(5L)).thenReturn(List.of(respuesta()));
        mockMvc.perform(get("/api/deseos").principal(TestFixtures.auth("cliente@mail.com", "CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].juegoId").value(1));
    }

    @Test
    void agregarDeseoNuevoDevuelve201() throws Exception {
        when(service.agregar(5L, 1L)).thenReturn(new DeseosResult(true, List.of(respuesta())));
        mockMvc.perform(post("/api/deseos")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"juegoId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].titulo").value("Juego"));
    }

    @Test
    void agregarDeseoDuplicadoDevuelve200() throws Exception {
        when(service.agregar(5L, 1L)).thenReturn(new DeseosResult(false, List.of(respuesta())));
        mockMvc.perform(post("/api/deseos")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"juegoId\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void eliminarDeseoDevuelveLista() throws Exception {
        when(service.eliminar(eq(5L), eq(1L))).thenReturn(List.of());
        mockMvc.perform(delete("/api/deseos/1")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
