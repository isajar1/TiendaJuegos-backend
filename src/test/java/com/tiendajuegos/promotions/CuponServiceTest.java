package com.tiendajuegos.promotions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class CuponServiceTest {

    @Mock
    private CuponRepository repository;

    @Mock
    private AuditService auditService;

    private CuponService service;

    @BeforeEach
    void setUp() {
        service = new CuponService(repository, auditService);
    }

    @Test
    void descuentoAplicaElPorcentaje() {
        Cupon cupon = new Cupon("BIENVENIDO", new BigDecimal("10.00"),
                LocalDateTime.now().plusDays(1), true);

        assertEquals(new BigDecimal("15.00"), service.descuento(new BigDecimal("150.00"), cupon));
    }

    @Test
    void validarRechazaCuponVencido() {
        Cupon vencido = new Cupon("VIEJO", new BigDecimal("10.00"),
                LocalDateTime.now().minusDays(1), true);
        when(repository.findByCodigoIgnoreCase("VIEJO")).thenReturn(Optional.of(vencido));

        ApiException exception = assertThrows(ApiException.class, () -> service.validar("VIEJO"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.status());
    }
}
