package com.tiendajuegos.social;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.catalog.Juego;
import com.tiendajuegos.catalog.JuegoRepository;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUser;
import com.tiendajuegos.dto.ApiDtos.ResenaRequest;
import com.tiendajuegos.sales.DetalleOrdenRepository;
import com.tiendajuegos.sales.EstadoPago;
import com.tiendajuegos.user.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    @Mock private ResenaRepository resenaRepository;
    @Mock private JuegoRepository juegoRepository;
    @Mock private DetalleOrdenRepository detalleRepository;
    @Mock private CurrentUser currentUser;
    @Mock private AuditService auditService;

    @InjectMocks private ResenaService resenaService;

    @Test
    void listar_juegoExisteYNoExiste() {
        when(juegoRepository.existsById(99L)).thenReturn(false);
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ApiException.class, () -> resenaService.listar(99L)).status());

        Juego j = mock(Juego.class);
        when(j.getId()).thenReturn(1L);
        Usuario u = mock(Usuario.class);
        when(u.getCorreo()).thenReturn("user@test.com");
        Resena r = mock(Resena.class);
        when(r.getId()).thenReturn(10L);
        when(r.getJuego()).thenReturn(j);
        when(r.getUsuario()).thenReturn(u);
        when(r.getCalificacion()).thenReturn(5);
        when(r.getComentario()).thenReturn("Excelente");
        when(r.getFecha()).thenReturn(LocalDateTime.now());

        when(juegoRepository.existsById(1L)).thenReturn(true);
        when(resenaRepository.findByJuegoId(1L)).thenReturn(List.of(r));

        var lista = resenaService.listar(1L);
        assertEquals(1, lista.size());
        assertEquals(5, lista.get(0).calificacion());
    }

    @Test
    void crear_flujoExitosoYValidaciones() {
        // Calificacion invalida
        assertThrows(ApiException.class, () -> resenaService.crear(1L, null));
        assertThrows(ApiException.class, () -> resenaService.crear(1L, new ResenaRequest(0, "Malo")));
        assertThrows(ApiException.class, () -> resenaService.crear(1L, new ResenaRequest(6, "Malo")));

        // Juego no existe
        when(juegoRepository.findById(99L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ApiException.class, () -> resenaService.crear(99L, new ResenaRequest(5, "Ok"))).status());

        Juego juego = mock(Juego.class);
        when(juego.getId()).thenReturn(1L);
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(currentUser.id()).thenReturn(7L);

        // No compro el juego
        when(detalleRepository.existsByOrdenUsuarioIdAndOrdenEstadoPagoAndJuegoId(7L, EstadoPago.pagado, 1L))
                .thenReturn(false);
        assertEquals(HttpStatus.FORBIDDEN,
                assertThrows(ApiException.class, () -> resenaService.crear(1L, new ResenaRequest(5, "Ok"))).status());

        // Ya publico resena
        when(detalleRepository.existsByOrdenUsuarioIdAndOrdenEstadoPagoAndJuegoId(7L, EstadoPago.pagado, 1L))
                .thenReturn(true);
        when(resenaRepository.findByJuegoIdAndUsuarioId(1L, 7L)).thenReturn(Optional.of(mock(Resena.class)));
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ApiException.class, () -> resenaService.crear(1L, new ResenaRequest(5, "Ok"))).status());

        // Creacion exitosa
        when(resenaRepository.findByJuegoIdAndUsuarioId(1L, 7L)).thenReturn(Optional.empty());
        Usuario u = mock(Usuario.class);
        when(u.getCorreo()).thenReturn("gamer@test.com");
        when(currentUser.usuario()).thenReturn(u);
        when(resenaRepository.save(any(Resena.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = resenaService.crear(1L, new ResenaRequest(5, "Juegazo"));
        assertEquals("Juegazo", resp.comentario());
        verify(auditService).registrar("CREAR_RESENA", "resenas");
    }
}