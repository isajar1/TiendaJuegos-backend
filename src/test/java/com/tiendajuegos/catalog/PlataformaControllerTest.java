package com.tiendajuegos.catalog;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tiendajuegos.MvcTestSupport;
import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.catalog.CatalogDtos.NombreResponse;
import com.tiendajuegos.common.ApiException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
class PlataformaControllerTest {

    @Mock private PlataformaService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MvcTestSupport.mvc(new PlataformaController(service));
    }

    @Test
    void listarPlataformas() throws Exception {
        when(service.listar()).thenReturn(List.of(new NombreResponse(1L, "Steam")));
        mockMvc.perform(get("/api/plataformas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Steam"));
    }

    @Test
    void crearPlataformaDevuelve201() throws Exception {
        when(service.crear(any())).thenReturn(new NombreResponse(2L, "Xbox"));
        mockMvc.perform(post("/api/plataformas")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Xbox\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Xbox"));
    }

    @Test
    void crearDuplicadaDevuelve409() throws Exception {
        when(service.crear(any())).thenThrow(ApiException.conflict("La plataforma ya existe"));
        mockMvc.perform(post("/api/plataformas")
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Xbox\"}"))
                .andExpect(status().isConflict());
    }
}
