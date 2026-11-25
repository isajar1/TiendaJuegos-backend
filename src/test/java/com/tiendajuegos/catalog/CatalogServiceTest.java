package com.tiendajuegos.catalog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.dto.ApiDtos.CatalogoItem;
import com.tiendajuegos.dto.ApiDtos.JuegoRequest;
import com.tiendajuegos.dto.ApiDtos.JuegoResponse;
import com.tiendajuegos.inventory.ClaveDigitalRepository;
import com.tiendajuegos.inventory.EstadoClave;
import com.tiendajuegos.social.Resena;
import com.tiendajuegos.social.ResenaRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CatalogServiceTest {

    @Mock private JuegoRepository juegoRepository;
    @Mock private PlataformaRepository plataformaRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private ClaveDigitalRepository claveDigitalRepository;
    @Mock private ResenaRepository resenaRepository;
    @Mock private AuditService auditService;

    @InjectMocks private CatalogService catalogService;

    private Plataforma plataforma;
    private Categoria categoria;
    private Juego juego;

    @BeforeEach
    void setUp() {
        plataforma = new Plataforma("PC");
        categoria = new Categoria("Accion");
        juego = new Juego("Elden Ring", "RPG de mundo abierto", new BigDecimal("59.99"), "Global", plataforma, categoria);
    }

    @Test
    void listar_conTodosLosFiltros_construyePredicadosYRetornaLista() {
        when(juegoRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(juego));
        when(resenaRepository.findByJuegoId(any())).thenReturn(List.of());
        when(claveDigitalRepository.countByJuegoIdAndEstado(any(), eq(EstadoClave.disponible))).thenReturn(3L);

        List<JuegoResponse> resultado = catalogService.listar(
                "Elden", 1L, 2L, "Global", new BigDecimal("10"), new BigDecimal("100"), EstadoJuego.activo);

        assertEquals(1, resultado.size());
        assertEquals("Elden Ring", resultado.get(0).titulo());
        assertTrue(resultado.get(0).disponible());

        ArgumentCaptor<Specification<Juego>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(juegoRepository).findAll(specCaptor.capture(), any(Sort.class));

        Root<Juego> root = mock(Root.class);
        Path<Object> path = mock(Path.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.get(anyString())).thenReturn(path);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        when(cb.lower(any())).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
        when(cb.like(any(), anyString())).thenReturn(mock(Predicate.class));
        when(cb.equal(any(), any())).thenReturn(mock(Predicate.class));
        when(cb.greaterThanOrEqualTo(any(), any(BigDecimal.class))).thenReturn(mock(Predicate.class));
        when(cb.lessThanOrEqualTo(any(), any(BigDecimal.class))).thenReturn(mock(Predicate.class));
        when(cb.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));

        specCaptor.getValue().toPredicate(root, query, cb);
    }

    @Test
    void listar_sinFiltros_retornaTodos() {
        when(juegoRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());
        List<JuegoResponse> resultado = catalogService.listar(null, null, null, "", null, null, null);
        assertTrue(resultado.isEmpty());

        ArgumentCaptor<Specification<Juego>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(juegoRepository).findAll(specCaptor.capture(), any(Sort.class));
        specCaptor.getValue().toPredicate(mock(Root.class), mock(CriteriaQuery.class), mock(CriteriaBuilder.class));
    }

    @Test
    void detalle_juegoExisteConResenas_calculaPromedioYDisponibilidad() {
        Resena r1 = mock(Resena.class);
        when(r1.getCalificacion()).thenReturn(5);
        Resena r2 = mock(Resena.class);
        when(r2.getCalificacion()).thenReturn(4);

        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(resenaRepository.findByJuegoId(any())).thenReturn(List.of(r1, r2));
        when(claveDigitalRepository.countByJuegoIdAndEstado(any(), eq(EstadoClave.disponible))).thenReturn(0L);

        JuegoResponse resp = catalogService.detalle(1L);

        assertEquals("Elden Ring", resp.titulo());
        assertEquals(4.5, resp.calificacionPromedio());
        assertEquals(2, resp.totalResenas());
        assertFalse(resp.disponible());
    }

    @Test
    void detalle_juegoNoExiste_lanzaNotFound() {
        when(juegoRepository.findById(99L)).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () -> catalogService.detalle(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.status());
    }

    @Test
    void crear_datosValidosConRegionVacia_asignaGlobalYAudita() {
        JuegoRequest req = new JuegoRequest("Halo", "Shooter", new BigDecimal("29.99"), " ", 1L, 2L);
        when(plataformaRepository.findById(1L)).thenReturn(Optional.of(plataforma));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
        when(juegoRepository.save(any(Juego.class))).thenAnswer(inv -> inv.getArgument(0));
        when(resenaRepository.findByJuegoId(any())).thenReturn(List.of());

        JuegoResponse resp = catalogService.crear(req);

        assertEquals("Halo", resp.titulo());
        assertEquals("Global", resp.region());
        verify(auditService).registrar("CREAR_JUEGO", "juegos");
    }

    @Test
    void crear_validacionesYErroresDeExistencia() {
        assertThrows(ApiException.class, () -> catalogService.crear(null));
        assertThrows(ApiException.class, () -> catalogService.crear(
                new JuegoRequest("", "desc", BigDecimal.TEN, "US", 1L, 1L)));
        assertThrows(ApiException.class, () -> catalogService.crear(
                new JuegoRequest("Juego", "desc", new BigDecimal("-1"), "US", 1L, 1L)));
        assertThrows(ApiException.class, () -> catalogService.crear(
                new JuegoRequest("Juego", "desc", BigDecimal.TEN, "US", null, 1L)));

        JuegoRequest reqValido = new JuegoRequest("Juego", "desc", BigDecimal.TEN, "LATAM", 1L, 2L);
        when(plataformaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> catalogService.crear(reqValido));

        when(plataformaRepository.findById(1L)).thenReturn(Optional.of(plataforma));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> catalogService.crear(reqValido));
    }

    @Test
    void actualizar_datosValidos_actualizaYAudita() {
        JuegoRequest req = new JuegoRequest("Elden Ring GOTY", "Nueva desc", new BigDecimal("49.99"), "LATAM", 1L, 2L);
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(plataformaRepository.findById(1L)).thenReturn(Optional.of(plataforma));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
        when(juegoRepository.save(any(Juego.class))).thenAnswer(inv -> inv.getArgument(0));

        JuegoResponse resp = catalogService.actualizar(1L, req);

        assertEquals("Elden Ring GOTY", resp.titulo());
        assertEquals("LATAM", resp.region());
        verify(auditService).registrar("EDITAR_JUEGO", "juegos");
    }

    @Test
    void deshabilitar_cambiaEstadoAInactivoYAudita() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        catalogService.deshabilitar(1L);
        assertEquals(EstadoJuego.inactivo, juego.getEstado());
        verify(juegoRepository).save(juego);
        verify(auditService).registrar("DESHABILITAR_JUEGO", "juegos");
    }

    @Test
    void categoriasYPlataformas_crudYValidaciones() {
        when(categoriaRepository.findAll(Sort.by("nombre"))).thenReturn(List.of(categoria));
        assertEquals(1, catalogService.categorias().size());

        when(plataformaRepository.findAll(Sort.by("nombre"))).thenReturn(List.of(plataforma));
        assertEquals(1, catalogService.plataformas().size());

        when(categoriaRepository.existsByNombreIgnoreCase("RPG")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));
        CatalogoItem cat = catalogService.crearCategoria("  RPG  ");
        assertEquals("RPG", cat.nombre());

        when(categoriaRepository.existsByNombreIgnoreCase("RPG")).thenReturn(true);
        assertThrows(ApiException.class, () -> catalogService.crearCategoria("RPG"));
        assertThrows(ApiException.class, () -> catalogService.crearCategoria(" "));

        when(plataformaRepository.existsByNombreIgnoreCase("Steam")).thenReturn(false);
        when(plataformaRepository.save(any(Plataforma.class))).thenAnswer(inv -> inv.getArgument(0));
        CatalogoItem plat = catalogService.crearPlataforma("Steam");
        assertEquals("Steam", plat.nombre());

        when(plataformaRepository.existsByNombreIgnoreCase("Steam")).thenReturn(true);
        assertThrows(ApiException.class, () -> catalogService.crearPlataforma("Steam"));
    }
}