package com.tiendajuegos.repository;

import com.tiendajuegos.domain.Resena;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    interface ResumenPorJuego {
        Long getJuegoId();

        Long getTotal();

        Double getPromedio();
    }

    /** Calificación y total en una sola consulta; antes se cargaban todas las reseñas por juego. */
    @Query("select r.juego.id as juegoId, count(r) as total, avg(r.calificacion) as promedio "
            + "from Resena r group by r.juego.id")
    List<ResumenPorJuego> resumenPorJuego();

    List<Resena> findByJuegoIdOrderByFechaDesc(Long juegoId);

    boolean existsByUsuarioIdAndJuegoId(Long usuarioId, Long juegoId);
}
