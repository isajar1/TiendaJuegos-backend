package com.tiendajuegos.repository;

import com.tiendajuegos.domain.CarritoItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {

    List<CarritoItem> findByCarritoIdOrderByIdAsc(Long carritoId);

    Optional<CarritoItem> findByCarritoIdAndJuegoId(Long carritoId, Long juegoId);
}
