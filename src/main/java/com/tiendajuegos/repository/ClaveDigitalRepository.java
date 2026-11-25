package com.tiendajuegos.repository;

import com.tiendajuegos.domain.ClaveDigital;
import com.tiendajuegos.domain.EstadoClave;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaveDigitalRepository extends JpaRepository<ClaveDigital, Long> {

    long countByJuegoIdAndEstado(Long juegoId, EstadoClave estado);

    Optional<ClaveDigital> findFirstByJuegoIdAndEstadoOrderByIdAsc(Long juegoId, EstadoClave estado);

    List<ClaveDigital> findByJuegoId(Long juegoId);
}
