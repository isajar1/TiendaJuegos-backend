package com.tiendajuegos.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.catalog.CatalogDtos.NombreRequest;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.domain.Categoria;
import com.tiendajuegos.repository.CategoriaRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock private CategoriaRepository repository;
    @InjectMocks private CategoriaService service;

    @Test
    void listarDevuelveCategorias() {
        when(repository.findAllByOrderByNombreAsc())
                .thenReturn(List.of(TestFixtures.categoria(1L, "Accion"), TestFixtures.categoria(2L, "RPG")));

        var respuesta = service.listar();

        assertEquals(2, respuesta.size());
        assertEquals("Accion", respuesta.get(0).nombre());
    }

    @Test
    void crearNormalizaYAgrega() {
        when(repository.existsByNombreIgnoreCase("Accion")).thenReturn(false);
        when(repository.save(any(Categoria.class)))
                .thenAnswer(invocation -> {
                    Categoria categoria = invocation.getArgument(0);
                    org.springframework.test.util.ReflectionTestUtils.setField(categoria, "id", 3L);
                    return categoria;
                });

        var respuesta = service.crear(new NombreRequest("  Accion  "));

        assertEquals(3L, respuesta.id());
        assertEquals("Accion", respuesta.nombre());
    }

    @Test
    void crearDuplicadaLanza409() {
        when(repository.existsByNombreIgnoreCase("Accion")).thenReturn(true);
        ApiException exception = assertThrows(ApiException.class,
                () -> service.crear(new NombreRequest("Accion")));
        assertEquals(409, exception.getStatus().value());
        verify(repository, never()).save(any());
    }
}
