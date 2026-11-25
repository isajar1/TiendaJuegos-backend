package com.tiendajuegos.inventory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.catalog.Juego;
import com.tiendajuegos.catalog.JuegoRepository;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.dto.ApiDtos.ClavesRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ClaveServiceTest {

    @Mock private ClaveDigitalRepository claveRepository;
    @Mock private JuegoRepository juegoRepository;
    @Mock private KeyCipherService cipher;
    @Mock private AuditService auditService;

    @InjectMocks private ClaveService claveService;

    @Test
    void cargar_clavesNuevasValidas_cifraGuardaYAudita() {
        Juego juego = mock(Juego.class);
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(claveRepository.findByJuegoId(1L)).thenReturn(List.of());
        when(cipher.cifrar("KEY-111")).thenReturn("ENC-111");
        when(cipher.cifrar("KEY-222")).thenReturn("ENC-222");

        int cargadas = claveService.cargar(1L, new ClavesRequest(List.of(" KEY-111 ", "KEY-222")));

        assertEquals(2, cargadas);
        verify(claveRepository).saveAll(any());
        verify(auditService).registrar("CARGA_CLAVES", "claves_digitales");
    }

    @Test
    void cargar_validacionesYDuplicados_lanzaExcepcion() {
        when(juegoRepository.findById(99L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ApiException.class, () -> claveService.cargar(99L, new ClavesRequest(List.of("A")))).status());

        Juego juego = mock(Juego.class);
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));

        // Lista nula o vacia
        assertThrows(ApiException.class, () -> claveService.cargar(1L, null));
        assertThrows(ApiException.class, () -> claveService.cargar(1L, new ClavesRequest(List.of())));

        // Clave en blanco o repetida en la misma peticion
        assertThrows(ApiException.class, () -> claveService.cargar(1L, new ClavesRequest(Arrays.asList("KEY-1", " "))));
        assertThrows(ApiException.class, () -> claveService.cargar(1L, new ClavesRequest(List.of("KEY-1", "KEY-1"))));

        // Clave ya existente en BD
        ClaveDigital existente = new ClaveDigital(juego, "ENC-EXISTE");
        when(claveRepository.findByJuegoId(1L)).thenReturn(List.of(existente));
        when(cipher.descifrar("ENC-EXISTE")).thenReturn("KEY-EXISTE");

        ApiException exConflicto = assertThrows(ApiException.class,
                () -> claveService.cargar(1L, new ClavesRequest(List.of("KEY-EXISTE"))));
        assertEquals(HttpStatus.CONFLICT, exConflicto.status());
    }

    @Test
    void descifrar_claveExisteYNoExiste() {
        ClaveDigital clave = new ClaveDigital(mock(Juego.class), "ENC-ABC");
        when(claveRepository.findById(5L)).thenReturn(Optional.of(clave));
        when(cipher.descifrar("ENC-ABC")).thenReturn("ABC-123");

        assertEquals("ABC-123", claveService.descifrar(5L));

        when(claveRepository.findById(404L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ApiException.class, () -> claveService.descifrar(404L)).status());
    }
}