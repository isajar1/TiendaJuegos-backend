package com.tiendajuegos.resena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.domain.EstadoPago;
import com.tiendajuegos.domain.Juego;
import com.tiendajuegos.domain.Resena;
import com.tiendajuegos.repository.DetalleOrdenRepository;
import com.tiendajuegos.repository.JuegoRepository;
import com.tiendajuegos.repository.ResenaRepository;
import com.tiendajuegos.resena.ResenaDtos.ResenaRequest;
import com.tiendajuegos.user.Role;
import com.tiendajuegos.user.Usuario;
import com.tiendajuegos.user.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    @Mock private ResenaRepository resenaRepository;
    @Mock private JuegoRepository juegoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private DetalleOrdenRepository detalleRepository;
    @Mock private AuditService auditService;

    private ResenaService service;
    private Juego juego;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        service = new ResenaService(resenaRepository, juegoRepository, usuarioRepository,
                detalleRepository, auditService);
        juego = TestFixtures.juego(1L, "Juego", BigDecimal.TEN);
        usuario = TestFixtures.usuario(5L, "Sarai", "sarai@mail.com", Role.cliente);
    }

    @Test
    void porJuegoMapeaNombreDelUsuario() {
        when(resenaRepository.findByJuegoIdOrderByFechaDesc(1L))
                .thenReturn(List.of(TestFixtures.resena(3L, juego, usuario, 5, "Excelente")));

        var respuesta = service.porJuego(1L);

        assertEquals("Sarai", respuesta.get(0).nombreUsuario());
        assertEquals(5, respuesta.get(0).calificacion());
        assertEquals("2026-01-15T10:00:00Z", respuesta.get(0).fecha());
    }

    @Test
    void crearConCompraPagadaGuardaResena() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(detalleRepository.existsPaidPurchase(5L, 1L, EstadoPago.pagado)).thenReturn(true);
        when(resenaRepository.existsByUsuarioIdAndJuegoId(5L, 1L)).thenReturn(false);
        when(usuarioRepository.getReferenceById(5L)).thenReturn(usuario);
        when(resenaRepository.save(any(Resena.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var respuesta = service.crear(1L, 5L, new ResenaRequest(4, "Muy bueno"));

        assertEquals(4, respuesta.calificacion());
        assertEquals("Muy bueno", respuesta.comentario());
        assertEquals(5L, respuesta.usuarioId());
        verify(auditService).registrar(5L, "reseña creada", "resena");
    }

    @Test
    void crearSinCompraLanza409() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(detalleRepository.existsPaidPurchase(5L, 1L, EstadoPago.pagado)).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.crear(1L, 5L, new ResenaRequest(5, "Sin compra")));

        assertEquals(409, exception.getStatus().value());
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void crearResenaDuplicadaLanza409() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(detalleRepository.existsPaidPurchase(5L, 1L, EstadoPago.pagado)).thenReturn(true);
        when(resenaRepository.existsByUsuarioIdAndJuegoId(5L, 1L)).thenReturn(true);

        assertEquals(409, assertThrows(ApiException.class,
                () -> service.crear(1L, 5L, new ResenaRequest(3, "Otra"))).getStatus().value());
    }

    @Test
    void crearCalificacionFueraDeRangoLanza400() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        assertEquals(400, assertThrows(ApiException.class,
                () -> service.crear(1L, 5L, new ResenaRequest(6, "Malo"))).getStatus().value());
        assertEquals(400, assertThrows(ApiException.class,
                () -> service.crear(1L, 5L, new ResenaRequest(null, "Malo"))).getStatus().value());
    }

    @Test
    void crearJuegoInexistenteLanza404() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ApiException.class,
                () -> service.crear(1L, 5L, new ResenaRequest(5, "Hola"))).getStatus().value());
    }

    @Test
    void eliminarResenaPropia() {
        Resena resena = TestFixtures.resena(3L, juego, usuario, 5, "Bueno");
        when(resenaRepository.findById(3L)).thenReturn(Optional.of(resena));

        service.eliminar(3L, 5L, false);

        verify(resenaRepository).delete(resena);
        verify(auditService).registrar(5L, "reseña eliminada", "resena");
    }

    @Test
    void eliminarResenaAjenaComoClienteLanza403() {
        when(resenaRepository.findById(3L))
                .thenReturn(Optional.of(TestFixtures.resena(3L, juego, usuario, 5, "Bueno")));
        assertEquals(403, assertThrows(ApiException.class,
                () -> service.eliminar(3L, 6L, false)).getStatus().value());
        verify(resenaRepository, never()).delete(any());
    }

    @Test
    void eliminarResenaAjenaComoAdmin() {
        when(resenaRepository.findById(3L))
                .thenReturn(Optional.of(TestFixtures.resena(3L, juego, usuario, 5, "Bueno")));
        service.eliminar(3L, 6L, true);
        verify(resenaRepository).delete(any(Resena.class));
    }

    @Test
    void eliminarResenaInexistenteLanza404() {
        when(resenaRepository.findById(3L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ApiException.class,
                () -> service.eliminar(3L, 5L, false)).getStatus().value());
    }
}
