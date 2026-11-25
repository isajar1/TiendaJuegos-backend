package com.tiendajuegos.catalog;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tiendajuegos.MvcTestSupport;
import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.catalog.CatalogDtos.ClavesDisponiblesResponse;
import com.tiendajuegos.catalog.CatalogDtos.ClavesResponse;
import com.tiendajuegos.catalog.CatalogDtos.JuegoResponse;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUserService;
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
class JuegoControllerTest {

    @Mock private JuegoService service;
    @Mock private CurrentUserService currentUserService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MvcTestSupport.mvc(new JuegoController(service, currentUserService));
    }

    private JuegoResponse respuesta() {
        return new JuegoResponse(1L, "Juego", "Descripción", new BigDecimal("19.90"), "Global",
                "Steam", "Accion", true, 3L, 4.5, 2L);
    }

    @Test
    void listarExponeActivoYClavesDisponibles() throws Exception {
        when(service.listar(null, null, null, null)).thenReturn(List.of(respuesta()));

        mockMvc.perform(get("/api/juegos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Juego"))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[0].clavesDisponibles").value(3))
                .andExpect(jsonPath("$[0].calificacionPromedio").value(4.5));
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        when(service.obtener(99L)).thenThrow(ApiException.notFound("Juego no encontrado"));
        mockMvc.perform(get("/api/juegos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Juego no encontrado"));
    }

    @Test
    void crearJuegoDevuelve201() throws Exception {
        when(currentUserService.id(any())).thenReturn(9L);
        when(service.crear(any(), eq(9L))).thenReturn(respuesta());

        mockMvc.perform(post("/api/juegos")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Juego\",\"descripcion\":\"Descripción\","
                                + "\"precio\":19.90,\"region\":\"Global\","
                                + "\"plataformaId\":1,\"categoriaId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).crear(any(), eq(9L));
    }

    @Test
    void crearJuegoConDatosInvalidosDevuelve400() throws Exception {
        mockMvc.perform(post("/api/juegos")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"precio\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void actualizarJuegoDevuelve200() throws Exception {
        when(currentUserService.id(any())).thenReturn(9L);
        when(service.actualizar(eq(1L), any(), eq(9L))).thenReturn(respuesta());

        mockMvc.perform(put("/api/juegos/1")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Juego\",\"precio\":19.90,\"region\":\"Global\","
                                + "\"plataformaId\":1,\"categoriaId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Juego"));
    }

    @Test
    void desactivarJuegoDevuelve204() throws Exception {
        when(currentUserService.id(any())).thenReturn(9L);
        mockMvc.perform(delete("/api/juegos/1")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN")))
                .andExpect(status().isNoContent());
        verify(service).desactivar(1L, 9L);
    }

    @Test
    void agregarClavesDevuelve201() throws Exception {
        when(currentUserService.id(any())).thenReturn(9L);
        when(service.agregarClaves(eq(1L), any(), eq(9L))).thenReturn(new ClavesResponse(2));

        mockMvc.perform(post("/api/juegos/1/claves")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"claves\":[\"ABC\",\"DEF\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.creadas").value(2));
    }

    @Test
    void contarClavesDevuelveDisponiblesYVendidas() throws Exception {
        when(service.contarClaves(1L)).thenReturn(new ClavesDisponiblesResponse(4, 6));

        mockMvc.perform(get("/api/juegos/1/claves")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponibles").value(4))
                .andExpect(jsonPath("$.vendidas").value(6));
    }
}
