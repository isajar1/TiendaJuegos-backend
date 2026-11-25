package com.tiendajuegos.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    private static final String SECRET_VALIDO = "clave-secreta-super-segura-de-mas-de-32-caracteres-2026";

    @Test
    void constructor_secretCorto_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("corto", 60000));
    }

    @Test
    void generateYValidateToken_usuarioValido_retornaTrue() {
        JwtService jwtService = new JwtService(SECRET_VALIDO, 60000);
        UserDetails user = new User("cliente@tienda.com", "pass", List.of());

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("cliente@tienda.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));

        UserDetails otroUser = new User("otro@tienda.com", "pass", List.of());
        assertFalse(jwtService.isTokenValid(token, otroUser));
    }
}