package com.tiendajuegos.audit;

import com.tiendajuegos.domain.LogAuditoria;
import com.tiendajuegos.repository.LogAuditoriaRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final LogAuditoriaRepository repository;

    public AuditService(LogAuditoriaRepository repository) {
        this.repository = repository;
    }

    public void registrar(Long usuarioId, String accion, String entidad) {
        repository.save(new LogAuditoria(usuarioId, accion, entidad, Instant.now()));
    }
}
