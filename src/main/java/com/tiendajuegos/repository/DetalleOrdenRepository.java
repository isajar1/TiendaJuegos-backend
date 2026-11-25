package com.tiendajuegos.repository;

import com.tiendajuegos.domain.DetalleOrden;
import com.tiendajuegos.domain.EstadoPago;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {

    List<DetalleOrden> findByOrdenIdOrderByIdAsc(Long ordenId);

    @Query("select count(d) > 0 from DetalleOrden d "
            + "where d.orden.usuario.id = :usuarioId and d.juego.id = :juegoId "
            + "and d.orden.estadoPago = :estado")
    boolean existsPaidPurchase(@Param("usuarioId") Long usuarioId,
                               @Param("juegoId") Long juegoId,
                               @Param("estado") EstadoPago estado);
}
