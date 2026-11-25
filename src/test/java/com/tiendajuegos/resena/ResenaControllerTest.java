package com.tiendajuegos.resena;

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
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUserService;
import com.tiendajuegos.resena.ResenaDtos.ResenaRequest;
import com.tiendajuegos.resena.ResenaDtos.ResenaResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
class ResenaControllerTest {

    @Mock private ResenaService service;
    @Mock private CurrentUserService currentUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MvcTestSupport.mvc(new ResenaController(service, currentUserService));
        org.mockito.Mockito.lenient().when(currentUserService.id(any())).thenReturn(5L);
    }

    private ResenaResponse respuesta() {
        return new ResenaResponse(3L, 1L, 5L, "Sarai", 5, "Excelente", "2026-01-15T10:00:00Z");
    }

    @Test
    void listarResenasEsPublico() throws Exception {
        when(service.porJuego(1L)).thenReturn(List.of(respuesta()));
        mockMvc.perform(get("/api/juegos/1/resenas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreUsuario").value("Sarai"));
    }

    @Test
    void crearResenaDevuelve201() throws Exception {
        when(service.crear(eq(1L), eq(5L), any(ResenaRequest.class))).thenReturn(respuesta());
        mockMvc.perform(post("/api/juegos/1/resenas")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"calificacion\":5,\"comentario\":\"Excelente\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calificacion").value(5));
    }

    @Test
    void crearResenaSinCompraDevuelve409() throws Exception {
        when(service.crear(eq(1L), eq(5L), any(ResenaRequest.class)))
                .thenThrow(ApiException.conflict("Debes comprar este juego para poder reseñarlo"));
        mockMvc.perform(post("/api/juegos/1/resenas")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"calificacion\":5,\"comentario\":\"Sin compra\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Debes comprar este juego para poder reseñarlo"));
    }

    @Test
    void crearResenaConCalificacionInvalidaDevuelve400() throws Exception {
        mockMvc.perform(post("/api/juegos/1/resenas")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"calificacion\":9,\"comentario\":\"Mal\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarResenaDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/resenas/3")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE")))
                .andExpect(status().isNoContent());
        verify(service).eliminar(3L, 5L, false);
    }

    @Test
    void eliminarResenaAjenaDevuelve403() throws Exception {
        org.mockito.Mockito.doThrow(ApiException.forbidden("No puedes eliminar la reseña de otro usuario"))
                .when(service).eliminar(3L, 5L, false);
        mockMvc.perform(delete("/api/resenas/3")
                        .principal(TestFixtures.auth("cliente@mail.com", "CLIENTE")))
                .andExpect(status().isForbidden());
    }
}
