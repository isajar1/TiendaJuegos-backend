package com.tiendajuegos.sales;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {
    List<DetalleOrden> findByOrdenId(Long ordenId);
    boolean existsByOrdenUsuarioIdAndOrdenEstadoPagoAndJuegoId(
            Long usuarioId, EstadoPago estadoPago, Long juegoId);
}
