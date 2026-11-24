package com.tiendajuegos.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JuegoRepository extends JpaRepository<Juego, Long>, JpaSpecificationExecutor<Juego> {
}
