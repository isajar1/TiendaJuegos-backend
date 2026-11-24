package com.tiendajuegos.social;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.catalog.Juego;
import com.tiendajuegos.catalog.JuegoRepository;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUser;
import com.tiendajuegos.dto.ApiDtos.ResenaRequest;
import com.tiendajuegos.sales.DetalleOrdenRepository;
import com.tiendajuegos.sales.EstadoPago;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    @Mock
    private ResenaRepository resenaRepository;
    @Mock
    private JuegoRepository juegoRepository;
    @Mock
    private DetalleOrdenRepository detalleRepository;
    @Mock
    private CurrentUser currentUser;
    @Mock
    private AuditService auditService;
    @Mock
    private Juego juego;

    private ResenaService service;

    @BeforeEach
    void setUp() {
        service = new ResenaService(resenaRepository, juegoRepository, detalleRepository,
                currentUser, auditService);
    }

    @Test
    void clienteSinCompraNoPuedeCalificar() {
        when(juegoRepository.findById(1L)).thenReturn(Optional.of(juego));
        when(currentUser.id()).thenReturn(9L);
        when(detalleRepository.existsByOrdenUsuarioIdAndOrdenEstadoPagoAndJuegoId(
                9L, EstadoPago.pagado, 1L)).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.crear(1L, new ResenaRequest(5, "Excelente")));

        assertEquals(HttpStatus.FORBIDDEN, exception.status());
    }
}
