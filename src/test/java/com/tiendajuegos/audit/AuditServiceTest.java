package com.tiendajuegos.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.tiendajuegos.domain.LogAuditoria;
import com.tiendajuegos.repository.LogAuditoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private LogAuditoriaRepository repository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void registrarGuardaAccionEntidadYFecha() {
        auditService.registrar(7L, "pago aprobado", "orden");

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(repository).save(captor.capture());
        LogAuditoria log = captor.getValue();
        assertEquals(7L, log.getUsuarioId());
        assertEquals("pago aprobado", log.getAccion());
        assertEquals("orden", log.getEntidad());
        assertNotNull(log.getFechaHora());
    }

    @Test
    void registrarAdmiteUsuarioNulo() {
        auditService.registrar(null, "reporte", "reporte");
        verify(repository).save(any(LogAuditoria.class));
    }
}
