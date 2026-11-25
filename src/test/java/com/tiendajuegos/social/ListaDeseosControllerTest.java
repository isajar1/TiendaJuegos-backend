package com.tiendajuegos.social;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.catalog.Juego;
import com.tiendajuegos.catalog.JuegoRepository;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUser;
import com.tiendajuegos.user.Usuario;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ListaDeseosControllerTest {

    @Mock private ListaDeseosRepository repository;
    @Mock private JuegoRepository juegoRepository;
    @Mock private CurrentUser currentUser;
    @Mock private AuditService auditService;

    @InjectMocks private ListaDeseosController controller;

    @Test
    void listarAgregarYQuitar_flujoCompleto() {
        when(currentUser.id()).thenReturn(3L);
        Juego j = mock(Juego.class);
        when(j.getId()).thenReturn(10L);
        when(j.getTitulo()).thenReturn("Cyberpunk");
        Usuario u = mock(Usuario.class);
        when(u.getId()).thenReturn(3L);
        when(currentUser.usuario()).thenReturn(u);

        // Listar
        ListaDeseos deseo = new ListaDeseos(u, j);
        when(repository.findByIdUsuarioId(3L)).thenReturn(List.of(deseo));
        assertEquals(1, controller.listar().size());

        // Agregar exitoso
        when(juegoRepository.findById(10L)).thenReturn(Optional.of(j));
        when(repository.existsById(any(ListaDeseosId.class))).thenReturn(false);
        var resp = controller.agregar(Map.of("juegoId", 10L));
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        verify(auditService).registrar("AGREGAR_DESEO", "listas_deseos");

        // Agregar duplicado
        when(repository.existsById(any(ListaDeseosId.class))).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ApiException.class, () -> controller.agregar(Map.of("juegoId", 10L))).status());

        // Agregar juego inexistente
        when(juegoRepository.findById(null)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ApiException.class, () -> controller.agregar(null)).status());

        // Quitar exitoso y no encontrado
        when(repository.existsById(any(ListaDeseosId.class))).thenReturn(true);
        assertEquals(HttpStatus.NO_CONTENT, controller.quitar(10L).getStatusCode());

        when(repository.existsById(any(ListaDeseosId.class))).thenReturn(false);
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ApiException.class, () -> controller.quitar(10L)).status());
    }
}