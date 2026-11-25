package com.tiendajuegos.audit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tiendajuegos.common.CurrentUser;
import com.tiendajuegos.user.Usuario;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private LogAuditoriaRepository repository;
    @Mock private CurrentUser currentUser;

    @InjectMocks private AuditService auditService;

    @Test
    void registrar_conUsuarioAutenticadoYSinAutenticar_guardaLog() {
        Usuario u = mock(Usuario.class);
        when(currentUser.usuario()).thenReturn(u);
        auditService.registrar("LOGIN", "usuarios");
        verify(repository, times(1)).save(any(LogAuditoria.class));

        // Cuando no hay usuario autenticado (lanza excepcion)
        when(currentUser.usuario()).thenThrow(new RuntimeException("No autenticado"));
        auditService.registrar("CONSULTA_PUBLICA", "juegos");
        verify(repository, times(2)).save(any(LogAuditoria.class));
    }

    @Test
    void listar_mapeaRegistrosConYSinUsuario() {
        Usuario u = mock(Usuario.class);
        when(u.getCorreo()).thenReturn("admin@tienda.com");

        LogAuditoria conUser = new LogAuditoria(u, "CREAR", "juegos");
        LogAuditoria sinUser = new LogAuditoria(null, "HEALTH", "sistema");

        when(repository.findAll(any(Sort.class))).thenReturn(List.of(conUser, sinUser));

        var lista = auditService.listar();
        assertEquals(2, lista.size());
        assertEquals("admin@tienda.com", lista.get(0).usuario());
        assertNull(lista.get(1).usuario());
    }
}