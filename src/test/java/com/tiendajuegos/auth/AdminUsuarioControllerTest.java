package com.tiendajuegos.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tiendajuegos.MvcTestSupport;
import com.tiendajuegos.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
class AdminUsuarioControllerTest {

    private static final String URL = "/api/admin/usuarios/administradores";
    private static final String CUERPO_VALIDO =
            "{\"nombre\":\"Nuevo\",\"correo\":\"nuevo@mail.com\",\"contrasena\":\"password123\"}";

    @Mock private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MvcTestSupport.mvc(new AdminUsuarioController(authService));
    }

    @Test
    void crearAdministradorDevuelve201SinExponerToken() throws Exception {
        when(authService.crearAdmin(any(), any())).thenReturn(
                new AuthResponse(3L, "Nuevo", "nuevo@mail.com", "admin", "jwt-token"));

        mockMvc.perform(post(URL)
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.rol").value("admin"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void crearAdministradorConCorreoDuplicadoDevuelve409() throws Exception {
        when(authService.crearAdmin(any(), any())).thenThrow(new EmailAlreadyRegisteredException());

        mockMvc.perform(post(URL)
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void crearAdministradorConDatosInvalidosDevuelve400() throws Exception {
        mockMvc.perform(post(URL)
                        .principal(TestFixtures.auth("admin@mail.com", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo\",\"correo\":\"nuevo@mail.com\",\"contrasena\":\"corta\"}"))
                .andExpect(status().isBadRequest());
    }
}
