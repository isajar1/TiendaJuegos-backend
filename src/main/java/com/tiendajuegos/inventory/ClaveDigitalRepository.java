package com.tiendajuegos.inventory;

import com.tiendajuegos.catalog.Juego;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaveDigitalRepository extends JpaRepository<ClaveDigital, Long> {
    Optional<ClaveDigital> findFirstByJuegoIdAndEstado(Long juegoId, EstadoClave estado);
    List<ClaveDigital> findByJuegoId(Long juegoId);
    long countByJuegoIdAndEstado(Long juegoId, EstadoClave estado);
}
