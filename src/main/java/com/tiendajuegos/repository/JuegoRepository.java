package com.tiendajuegos.repository;

import com.tiendajuegos.domain.Juego;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JuegoRepository extends JpaRepository<Juego, Long> {

    List<Juego> findAllByOrderByIdAsc();
}
