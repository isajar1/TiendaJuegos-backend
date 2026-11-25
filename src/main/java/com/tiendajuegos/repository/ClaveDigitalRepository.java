package com.tiendajuegos.repository;

import com.tiendajuegos.domain.ClaveDigital;
import com.tiendajuegos.domain.EstadoClave;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClaveDigitalRepository extends JpaRepository<ClaveDigital, Long> {

    interface ConteoPorJuego {
        Long getJuegoId();

        Long getTotal();
    }

    /** Conteo agrupado: una consulta para todo el catálogo en lugar de una por juego. */
    @Query("select c.juego.id as juegoId, count(c) as total from ClaveDigital c "
            + "where c.estado = :estado group by c.juego.id")
    List<ConteoPorJuego> contarPorEstado(@Param("estado") EstadoClave estado);

    long countByJuegoIdAndEstado(Long juegoId, EstadoClave estado);

    Optional<ClaveDigital> findFirstByJuegoIdAndEstadoOrderByIdAsc(Long juegoId, EstadoClave estado);

    List<ClaveDigital> findByJuegoId(Long juegoId);
}
