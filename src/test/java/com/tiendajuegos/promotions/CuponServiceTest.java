package com.tiendajuegos.promotions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.dto.ApiDtos.CuponRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class CuponServiceTest {

    @Mock private CuponRepository repository;
    @Mock private AuditService auditService;

    @InjectMocks private CuponService cuponService;

    @Test
    void validarYDescuento_casosValidosEInvalidos() {
        assertThrows(ApiException.class, () -> cuponService.validar(" "));

        when(repository.findByCodigoIgnoreCase("NOEXISTE")).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> cuponService.validar("NOEXISTE"));

        Cupon inactivo = new Cupon("OFF10", BigDecimal.TEN, LocalDateTime.now().plusDays(5), false);
        when(repository.findByCodigoIgnoreCase("OFF10")).thenReturn(Optional.of(inactivo));
        assertThrows(ApiException.class, () -> cuponService.validar("OFF10"));

        Cupon vencido = new Cupon("OLD20", BigDecimal.valueOf(20), LocalDateTime.now().minusDays(1), true);
        when(repository.findByCodigoIgnoreCase("OLD20")).thenReturn(Optional.of(vencido));
        assertThrows(ApiException.class, () -> cuponService.validar("OLD20"));

        Cupon valido = new Cupon("GAMER25", BigDecimal.valueOf(25), LocalDateTime.now().plusDays(10), true);
        when(repository.findByCodigoIgnoreCase("gamer25")).thenReturn(Optional.of(valido));
        assertEquals(valido, cuponService.validar(" gamer25 "));
    }

    @Test
    void listarCrearYDesactivar_flujoCompleto() {
        Cupon c = new Cupon("SAVE15", BigDecimal.valueOf(15), LocalDateTime.now().plusDays(3), true);
        when(repository.findAll(Sort.by("codigo"))).thenReturn(List.of(c));
        assertEquals(1, cuponService.listar().size());

        // Validaciones de creacion
        assertThrows(ApiException.class, () -> cuponService.crear(null));
        assertThrows(ApiException.class, () -> cuponService.crear(new CuponRequest("", BigDecimal.TEN, LocalDateTime.now(), true)));
        assertThrows(ApiException.class, () -> cuponService.crear(new CuponRequest("A", BigDecimal.ZERO, LocalDateTime.now(), true)));
        assertThrows(ApiException.class, () -> cuponService.crear(new CuponRequest("A", BigDecimal.valueOf(101), LocalDateTime.now(), true)));
        assertThrows(ApiException.class, () -> cuponService.crear(new CuponRequest("A", BigDecimal.TEN, null, true)));

        // Duplicado
        when(repository.existsByCodigoIgnoreCase("SAVE15")).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT, assertThrows(ApiException.class,
                () -> cuponService.crear(new CuponRequest("SAVE15", BigDecimal.TEN, LocalDateTime.now().plusDays(1), null))).status());

        // Crear valido
        when(repository.existsByCodigoIgnoreCase("NEW50")).thenReturn(false);
        when(repository.save(any(Cupon.class))).thenAnswer(inv -> inv.getArgument(0));
        var creado = cuponService.crear(new CuponRequest("NEW50", BigDecimal.valueOf(50), LocalDateTime.now().plusDays(2), null));
        assertTrue(creado.activo());

        // Desactivar
        when(repository.findById(1L)).thenReturn(Optional.of(c));
        cuponService.desactivar(1L);
        assertFalse(c.isActivo());

        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ApiException.class, () -> cuponService.desactivar(99L)).status());
    }
}