package com.tiendajuegos.sales;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    @Query("select o from Orden o where o.usuario.id = :usuarioId order by o.fechaCreacion desc")
    List<Orden> findByUsuario(@Param("usuarioId") Long usuarioId);

    @Query("""
            select j.titulo, count(d.id), coalesce(sum(d.precioUnitario), 0.0)
            from DetalleOrden d join d.orden o join d.juego j
            where o.estadoPago in :estados and o.fechaCreacion between :desde and :hasta
            group by j.titulo order by count(d.id) desc
            """)
    List<Object[]> ventasPorJuego(@Param("estados") List<EstadoPago> estados,
                                  @Param("desde") LocalDateTime desde,
                                  @Param("hasta") LocalDateTime hasta);

    @Query("""
            select u.correo, count(o.id), coalesce(sum(o.total), 0.0)
            from Orden o join o.usuario u
            where o.estadoPago in :estados and o.fechaCreacion between :desde and :hasta
            group by u.correo order by sum(o.total) desc
            """)
    List<Object[]> comprasPorCliente(@Param("estados") List<EstadoPago> estados,
                                     @Param("desde") LocalDateTime desde,
                                     @Param("hasta") LocalDateTime hasta);
}
