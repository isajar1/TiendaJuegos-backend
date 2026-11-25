package com.tiendajuegos.repository;

import com.tiendajuegos.domain.Resena;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByJuegoIdOrderByFechaDesc(Long juegoId);

    boolean existsByUsuarioIdAndJuegoId(Long usuarioId, Long juegoId);
}
