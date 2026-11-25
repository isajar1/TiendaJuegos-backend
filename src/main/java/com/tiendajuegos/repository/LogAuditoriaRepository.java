package com.tiendajuegos.repository;

import com.tiendajuegos.domain.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
}
