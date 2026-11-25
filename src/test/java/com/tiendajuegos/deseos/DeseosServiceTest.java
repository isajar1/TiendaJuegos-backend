package com.tiendajuegos.deseos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.domain.Juego;
import com.tiendajuegos.domain.ListaDeseos;
import com.tiendajuegos.domain.ListaDeseosId;
import com.tiendajuegos.repository.JuegoRepository;
import com.tiendajuegos.repository.ListaDeseosRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeseosServiceTest {

    @Mock private ListaDeseosRepository listaRepository;
    @Mock private JuegoRepository juegoRepository;
    @Mock private AuditService auditService;

    private DeseosService service;
    private Juego juego;
    private ListaDeseosId id;

    @BeforeEach
    void setUp() {
        service = new DeseosService(listaRepository, juegoRepository, auditService);
        juego = TestFixtures.juego(1L, "Juego deseado", new BigDecimal("49.90"));
        id = new ListaDeseosId(5L, 1L);
    }

    @Test
    void listarVacioDevuelveListaVacia() {
        when(listaRepository.findByUsuario(5L)).thenReturn(List.of());
        assertTrue(service.listar(5L).isEmpty());
    }

    @Test
    void listarMapeaJuegos() {
        when(listaRepository.findByUsuario(5L)).thenReturn(List.of(TestFixtures.deseo(5L, 1L)));
        when(juegoRepository.findAllById(List.of(1L))).thenReturn(List.of(juego));

        var respuesta = service.listar(5L);

        assertEquals(1, respuesta.size());
        assertEquals("Juego deseado", respuesta.get(0).titulo());
        assertEquals(new BigDecimal("49.90"), respuesta.get(0).precio());
    }

    @Test
    void agregarNuevoDevuelveCreadoYAudita() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(listaRepository.findById(id)).thenReturn(Optional.empty());
        when(listaRepository.findByUsuario(5L)).thenReturn(List.of(TestFixtures.deseo(5L, 1L)));
        when(juegoRepository.findAllById(List.of(1L))).thenReturn(List.of(juego));

        var respuesta = service.agregar(5L, 1L);

        assertTrue(respuesta.creado());
        assertEquals(1, respuesta.deseos().size());
        verify(listaRepository).save(any(ListaDeseos.class));
        verify(auditService).registrar(5L, "juego agregado a deseos", "lista_deseos");
    }

    @Test
    void agregarDuplicadoEsIdempotente() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(listaRepository.findById(id)).thenReturn(Optional.of(TestFixtures.deseo(5L, 1L)));
        when(listaRepository.findByUsuario(5L)).thenReturn(List.of());

        var respuesta = service.agregar(5L, 1L);

        assertFalse(respuesta.creado());
        verify(listaRepository, never()).save(any());
        verify(auditService, never()).registrar(any(), any(), any());
    }

    @Test
    void agregarJuegoInexistenteLanza404() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ApiException.class,
                () -> service.agregar(5L, 1L)).getStatus().value());
    }

    @Test
    void eliminarDeseoExistenteAudita() {
        when(listaRepository.findById(id)).thenReturn(Optional.of(TestFixtures.deseo(5L, 1L)));
        when(listaRepository.findByUsuario(5L)).thenReturn(List.of());

        var respuesta = service.eliminar(5L, 1L);

        assertTrue(respuesta.isEmpty());
        verify(listaRepository).delete(any(ListaDeseos.class));
        verify(auditService).registrar(5L, "juego eliminado de deseos", "lista_deseos");
    }

    @Test
    void eliminarDeseoInexistenteNoAudita() {
        when(listaRepository.findById(id)).thenReturn(Optional.empty());
        when(listaRepository.findByUsuario(5L)).thenReturn(List.of());

        service.eliminar(5L, 1L);

        verify(listaRepository, never()).delete(any());
        verify(auditService, never()).registrar(any(), any(), any());
    }
}
