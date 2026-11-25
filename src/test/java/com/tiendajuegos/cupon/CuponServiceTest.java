package com.tiendajuegos.cupon;

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
import com.tiendajuegos.cupon.CuponDtos.ActivoRequest;
import com.tiendajuegos.cupon.CuponDtos.CuponRequest;
import com.tiendajuegos.domain.Cupon;
import com.tiendajuegos.repository.CuponRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CuponServiceTest {

    @Mock private CuponRepository repository;
    @Mock private AuditService auditService;
    @InjectMocks private CuponService service;

    private CuponRequest request(String fecha) {
        return new CuponRequest("verano10", new BigDecimal("10.00"), fecha, null);
    }

    @Test
    void listarMapeaCupones() {
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of(
                TestFixtures.cupon(1L, "VERANO10", new BigDecimal("10"), true)));

        var respuesta = service.listar();

        assertEquals(1, respuesta.size());
        assertEquals("VERANO10", respuesta.get(0).codigo());
        assertTrue(respuesta.get(0).activo());
    }

    @Test
    void crearNormalizaCodigoYActivaPorDefecto() {
        when(repository.existsByCodigo("VERANO10")).thenReturn(false);
        when(repository.save(any(Cupon.class))).thenAnswer(invocation -> {
            Cupon cupon = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(cupon, "id", 2L);
            return cupon;
        });

        var respuesta = service.crear(request(LocalDateTime.now().plusDays(5).toString()), 7L);

        assertEquals("VERANO10", respuesta.codigo());
        assertTrue(respuesta.activo());
        verify(auditService).registrar(7L, "cupón creado", "cupon");
    }

    @Test
    void crearAceptaFechaSoloDia() {
        when(repository.existsByCodigo("VERANO10")).thenReturn(false);
        when(repository.save(any(Cupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var respuesta = service.crear(request("2030-12-31"), 7L);

        assertTrue(respuesta.fechaExpiracion().startsWith("2030-12-31"));
    }

    @Test
    void crearDuplicadoLanza409() {
        when(repository.existsByCodigo("VERANO10")).thenReturn(true);
        ApiException exception = assertThrows(ApiException.class,
                () -> service.crear(request("2030-12-31"), 7L));
        assertEquals(409, exception.getStatus().value());
        verify(repository, never()).save(any());
    }

    @Test
    void crearConFechaInvalidaLanza400() {
        when(repository.existsByCodigo("VERANO10")).thenReturn(false);
        ApiException exception = assertThrows(ApiException.class,
                () -> service.crear(request("no-es-fecha"), 7L));
        assertEquals(400, exception.getStatus().value());
    }

    @Test
    void actualizarActivo() {
        Cupon cupon = TestFixtures.cupon(1L, "VERANO10", new BigDecimal("10"), true);
        when(repository.findById(1L)).thenReturn(Optional.of(cupon));
        when(repository.save(cupon)).thenReturn(cupon);

        var respuesta = service.actualizar(1L, new ActivoRequest(false), 7L);

        assertFalse(respuesta.activo());
        verify(auditService).registrar(7L, "cupón actualizado", "cupon");
    }

    @Test
    void actualizarInexistenteLanza404() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ApiException.class,
                () -> service.actualizar(1L, new ActivoRequest(true), 7L)).getStatus().value());
    }

    @Test
    void validarCuponVigente() {
        Cupon cupon = TestFixtures.cupon(1L, "VERANO10", new BigDecimal("10"), true);
        when(repository.findByCodigo("VERANO10")).thenReturn(Optional.of(cupon));

        assertEquals(cupon, service.validar(" verano10 "));
        assertEquals(new BigDecimal("10"), service.porcentaje(cupon));
    }

    @Test
    void validarCuponInactivoLanza400() {
        when(repository.findByCodigo("VIEJO")).thenReturn(Optional.of(
                TestFixtures.cupon(1L, "VIEJO", new BigDecimal("10"), false)));
        assertEquals(400, assertThrows(ApiException.class,
                () -> service.validar("VIEJO")).getStatus().value());
    }

    @Test
    void validarCuponExpiradoLanza400() {
        Cupon cupon = new Cupon("EXPIRADO", new BigDecimal("10"), LocalDateTime.now().minusDays(1), true);
        when(repository.findByCodigo("EXPIRADO")).thenReturn(Optional.of(cupon));
        assertThrows(ApiException.class, () -> service.validar("EXPIRADO"));
    }

    @Test
    void validarCuponInexistenteLanza400() {
        when(repository.findByCodigo("NADA")).thenReturn(Optional.empty());
        assertEquals(400, assertThrows(ApiException.class,
                () -> service.validar("NADA")).getStatus().value());
    }
}
