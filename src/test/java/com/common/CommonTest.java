package com.tiendajuegos.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tiendajuegos.user.Usuario;
import com.tiendajuegos.user.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;

class CommonTest {

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void globalExceptionHandler_mapeaTodasLasExcepciones() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        var r1 = handler.apiError(new ApiException(HttpStatus.NOT_FOUND, "No existe"));
        assertEquals(HttpStatus.NOT_FOUND, r1.getStatusCode());
        assertEquals("No existe", r1.getBody().message());

        assertEquals(HttpStatus.BAD_REQUEST, handler.validation(mock(MethodArgumentNotValidException.class)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, handler.unreadable(mock(HttpMessageNotReadableException.class)).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, handler.duplicated(new DataIntegrityViolationException("dup")).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, handler.denied(new AccessDeniedException("no")).getStatusCode());
    }

    @Test
    void currentUser_autenticadoYAnonimo() {
        UsuarioRepository repo = mock(UsuarioRepository.class);
        CurrentUser currentUser = new CurrentUser(repo);

        // Sin autenticacion -> 401
        assertThrows(ApiException.class, currentUser::correo);

        // Usuario anonimo -> 401
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of()));
        assertThrows(ApiException.class, currentUser::correo);

        // Autenticado valido
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("gamer@tienda.com", null, List.of()));
        Usuario u = mock(Usuario.class);
        when(u.getId()).thenReturn(42L);
        when(repo.findByCorreo("gamer@tienda.com")).thenReturn(Optional.of(u));

        assertEquals(42L, currentUser.id());

        // Usuario no encontrado en BD -> 401
        when(repo.findByCorreo("gamer@tienda.com")).thenReturn(Optional.empty());
        assertThrows(ApiException.class, currentUser::usuario);
    }
}