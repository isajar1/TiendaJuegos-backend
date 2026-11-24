package com.tiendajuegos.social;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByJuegoId(Long juegoId);
    Optional<Resena> findByJuegoIdAndUsuarioId(Long juegoId, Long usuarioId);
}
