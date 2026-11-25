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
import com.tiendajuegos.domain.Plataforma;
import com.tiendajuegos.repository.PlataformaRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlataformaServiceTest {

    @Mock private PlataformaRepository repository;
    @InjectMocks private PlataformaService service;

    @Test
    void listarDevuelvePlataformas() {
        when(repository.findAllByOrderByNombreAsc())
                .thenReturn(List.of(TestFixtures.plataforma(1L, "Steam")));

        assertEquals("Steam", service.listar().get(0).nombre());
    }

    @Test
    void crearGuardaPlataforma() {
        when(repository.existsByNombreIgnoreCase("Xbox")).thenReturn(false);
        when(repository.save(any(Plataforma.class)))
                .thenAnswer(invocation -> {
                    Plataforma plataforma = invocation.getArgument(0);
                    org.springframework.test.util.ReflectionTestUtils.setField(plataforma, "id", 4L);
                    return plataforma;
                });

        var respuesta = service.crear(new NombreRequest(" Xbox "));

        assertEquals(4L, respuesta.id());
        assertEquals("Xbox", respuesta.nombre());
    }

    @Test
    void crearDuplicadaLanza409() {
        when(repository.existsByNombreIgnoreCase("Xbox")).thenReturn(true);
        assertThrows(ApiException.class, () -> service.crear(new NombreRequest("Xbox")));
        verify(repository, never()).save(any());
    }
}
