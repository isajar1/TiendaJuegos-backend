package com.tiendajuegos.catalog;

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
import com.tiendajuegos.catalog.CatalogDtos.ClavesRequest;
import com.tiendajuegos.catalog.CatalogDtos.JuegoRequest;
import com.tiendajuegos.catalog.CatalogDtos.JuegoResponse;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.crypto.CryptoService;
import com.tiendajuegos.domain.ClaveDigital;
import com.tiendajuegos.domain.EstadoClave;
import com.tiendajuegos.domain.Juego;
import com.tiendajuegos.domain.Resena;
import com.tiendajuegos.repository.CategoriaRepository;
import com.tiendajuegos.repository.ClaveDigitalRepository;
import com.tiendajuegos.repository.JuegoRepository;
import com.tiendajuegos.repository.PlataformaRepository;
import com.tiendajuegos.repository.ResenaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JuegoServiceTest {

    @Mock private JuegoRepository juegoRepository;
    @Mock private PlataformaRepository plataformaRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private ClaveDigitalRepository claveRepository;
    @Mock private ResenaRepository resenaRepository;
    @Mock private CryptoService cryptoService;
    @Mock private AuditService auditService;

    private JuegoService service;

    @BeforeEach
    void setUp() {
        service = new JuegoService(juegoRepository, plataformaRepository, categoriaRepository,
                claveRepository, resenaRepository, cryptoService, auditService);
    }

    private JuegoRequest request() {
        return new JuegoRequest("Nuevo juego", "Descripción", new BigDecimal("49.90"), "Global", 1L, 1L);
    }

    @Test
    void listarFiltraInactivosYAplicaFiltros() {
        Juego juego = TestFixtures.juego(1L, "FIFA 26", new BigDecimal("59.90"));
        Juego otro = TestFixtures.juego(2L, "Otro juego", new BigDecimal("10.00"));
        when(juegoRepository.findAllByOrderByIdAsc()).thenReturn(List.of(juego, TestFixtures.inactivo(3L, "Viejo", BigDecimal.ONE), otro));
        when(claveRepository.countByJuegoIdAndEstado(1L, EstadoClave.disponible)).thenReturn(2L);
        List<Resena> resenas = List.of(
                TestFixtures.resena(1L, juego, TestFixtures.usuario(1L, "A", "a@mail.com", null), 4, "Bueno"),
                TestFixtures.resena(2L, juego, TestFixtures.usuario(2L, "B", "b@mail.com", null), 5, "Excelente"));
        when(resenaRepository.findByJuegoIdOrderByFechaDesc(1L)).thenReturn(resenas);

        List<JuegoResponse> respuesta = service.listar("fifa", "Accion", "Steam", "Global");

        assertEquals(1, respuesta.size());
        JuegoResponse item = respuesta.get(0);
        assertEquals("FIFA 26", item.titulo());
        assertEquals("Steam", item.plataforma());
        assertEquals("Accion", item.categoria());
        assertTrue(item.activo());
        assertEquals(2L, item.clavesDisponibles());
        assertEquals(4.5, item.calificacionPromedio());
        assertEquals(2L, item.totalResenas());
    }

    @Test
    void listarSinFiltrosDevuelveTodosLosActivos() {
        when(juegoRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of(TestFixtures.juego(1L, "A", BigDecimal.TEN),
                        TestFixtures.juego(2L, "B", BigDecimal.TEN)));

        assertEquals(2, service.listar(null, null, null, null).size());
    }

    @Test
    void obtenerInexistenteLanza404() {
        when(juegoRepository.findById(99L)).thenReturn(Optional.empty());
        ApiException exception = assertThrows(ApiException.class, () -> service.obtener(99L));
        assertEquals(404, exception.getStatus().value());
    }

    @Test
    void crearGuardaJuegoYAudita() {
        when(plataformaRepository.findById(1L)).thenReturn(Optional.of(TestFixtures.plataforma(1L, "Steam")));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(TestFixtures.categoria(1L, "Accion")));
        when(juegoRepository.save(any(Juego.class)))
                .thenAnswer(invocation -> {
                    Juego juego = invocation.getArgument(0);
                    org.springframework.test.util.ReflectionTestUtils.setField(juego, "id", 10L);
                    return juego;
                });

        JuegoResponse respuesta = service.crear(request(), 9L);

        assertEquals(10L, respuesta.id());
        assertEquals("Nuevo juego", respuesta.titulo());
        assertTrue(respuesta.activo());
        verify(auditService).registrar(9L, "juego creado", "juego");
    }

    @Test
    void crearSinPlataformaLanza404() {
        when(plataformaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> service.crear(request(), 9L));
        verify(juegoRepository, never()).save(any());
    }

    @Test
    void crearSinCategoriaLanza404() {
        when(plataformaRepository.findById(1L)).thenReturn(Optional.of(TestFixtures.plataforma(1L, "Steam")));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> service.crear(request(), 9L));
    }

    @Test
    void actualizarModificaElJuegoExistente() {
        Juego juego = TestFixtures.juego(1L, "Viejo", new BigDecimal("10.00"));
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(plataformaRepository.findById(1L)).thenReturn(Optional.of(TestFixtures.plataforma(1L, "Steam")));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(TestFixtures.categoria(1L, "Accion")));
        when(juegoRepository.save(juego)).thenReturn(juego);

        JuegoResponse respuesta = service.actualizar(1L, request(), 9L);

        assertEquals("Nuevo juego", respuesta.titulo());
        assertEquals(new BigDecimal("49.90"), juego.getPrecio());
        assertEquals("Global", juego.getRegion());
        verify(auditService).registrar(9L, "juego editado", "juego");
    }

    @Test
    void desactivarJuego() {
        Juego juego = TestFixtures.juego(1L, "Juego", BigDecimal.TEN);
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(juegoRepository.save(juego)).thenReturn(juego);

        service.desactivar(1L, 9L);

        assertFalse(juego.isActivo());
        verify(auditService).registrar(9L, "juego desactivado", "juego");
    }

    @Test
    void agregarClavesCifraCadaClave() {
        Juego juego = TestFixtures.juego(1L, "Juego", BigDecimal.TEN);
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(cryptoService.encrypt("ABC")).thenReturn("cifrada-1");
        when(cryptoService.encrypt("DEF")).thenReturn("cifrada-2");

        var respuesta = service.agregarClaves(1L, new ClavesRequest(List.of("ABC", "DEF")), 9L);

        assertEquals(2L, respuesta.creadas());
        ArgumentCaptor<ClaveDigital> captor = ArgumentCaptor.forClass(ClaveDigital.class);
        verify(claveRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals("cifrada-1", captor.getAllValues().get(0).getClaveCifrada());
        assertEquals("cifrada-2", captor.getAllValues().get(1).getClaveCifrada());
        assertEquals(EstadoClave.disponible, captor.getAllValues().get(0).getEstado());
        verify(auditService).registrar(9L, "claves cargadas", "clave_digital");
    }

    @Test
    void agregarClavesAJuegoInexistenteLanza404() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> service.agregarClaves(1L, new ClavesRequest(List.of("A")), 9L));
    }

    @Test
    void contarClavesDevuelveDisponiblesYVendidas() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(TestFixtures.juego(1L, "Juego", BigDecimal.TEN)));
        when(claveRepository.countByJuegoIdAndEstado(1L, EstadoClave.disponible)).thenReturn(3L);
        when(claveRepository.countByJuegoIdAndEstado(1L, EstadoClave.vendida)).thenReturn(7L);

        var respuesta = service.contarClaves(1L);

        assertEquals(3L, respuesta.disponibles());
        assertEquals(7L, respuesta.vendidas());
    }

}
