package com.tiendajuegos.repository;

import com.tiendajuegos.domain.Juego;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JuegoRepository extends JpaRepository<Juego, Long> {

    List<Juego> findAllByOrderByIdAsc();

    /** Carga categoría y plataforma en una sola consulta para evitar N+1 en el catálogo. */
    @Query("select j from Juego j left join fetch j.categoria left join fetch j.plataforma order by j.id")
    List<Juego> findAllConRelaciones();
}
