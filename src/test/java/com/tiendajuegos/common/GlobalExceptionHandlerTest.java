package com.tiendajuegos.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tiendajuegos.MvcTestSupport;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MvcTestSupport.mvc(new BoomController());
    }

    @Test
    void apiExceptionBadRequest() throws Exception {
        mockMvc.perform(get("/boom/bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dato inválido"));
    }

    @Test
    void apiExceptionNotFound() throws Exception {
        mockMvc.perform(get("/boom/notfound"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No encontrado"));
    }

    @Test
    void apiExceptionConflict() throws Exception {
        mockMvc.perform(get("/boom/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe"));
    }

    @Test
    void apiExceptionForbidden() throws Exception {
        mockMvc.perform(get("/boom/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acceso denegado"));
    }

    @Test
    void authenticationExceptionIsUnauthorized() throws Exception {
        mockMvc.perform(get("/boom/unauth"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autenticación requerida"));
    }

    @Test
    void validationErrorIsBadRequest() throws Exception {
        mockMvc.perform(post("/boom/validar").contentType("application/json").content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void unreadableJsonIsBadRequest() throws Exception {
        mockMvc.perform(post("/boom/validar").contentType("application/json").content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Solicitud inválida"));
    }

    @Test
    void dataIntegrityViolationIsConflict() throws Exception {
        mockMvc.perform(get("/boom/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "El registro ya existe o viola una restricción de datos"));
    }

    @Test
    void typeMismatchIsBadRequest() throws Exception {
        mockMvc.perform(get("/boom/tipo").param("numero", "no-es-numero"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Solicitud inválida"));
    }

    @Test
    void unexpectedErrorIsInternalServerError() throws Exception {
        mockMvc.perform(get("/boom/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error interno del servidor"));
    }

    @RestController
    static class BoomController {

        @GetMapping("/boom/bad")
        String bad() {
            throw ApiException.badRequest("Dato inválido");
        }

        @GetMapping("/boom/notfound")
        String notFound() {
            throw ApiException.notFound("No encontrado");
        }

        @GetMapping("/boom/conflict")
        String conflict() {
            throw ApiException.conflict("Ya existe");
        }

        @GetMapping("/boom/forbidden")
        String forbidden() {
            throw new AccessDeniedException("No");
        }

        @GetMapping("/boom/unauth")
        String unauth() {
            throw new AuthenticationCredentialsNotFoundException("No");
        }

        @PostMapping("/boom/validar")
        String validar(@Valid @RequestBody Cuerpo cuerpo) {
            return cuerpo.nombre();
        }

        @GetMapping("/boom/duplicate")
        String duplicate() {
            throw new DataIntegrityViolationException("duplicate key");
        }

        @GetMapping("/boom/tipo")
        String tipo(@org.springframework.web.bind.annotation.RequestParam Integer numero) {
            return "ok";
        }

        @GetMapping("/boom/unexpected")
        String unexpected() {
            throw new IllegalStateException("boom");
        }
    }

    record Cuerpo(@NotBlank String nombre) {
    }
}
