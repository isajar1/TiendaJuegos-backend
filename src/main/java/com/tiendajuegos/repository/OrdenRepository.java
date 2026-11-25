package com.tiendajuegos.repository;

import com.tiendajuegos.domain.EstadoPago;
import com.tiendajuegos.domain.Orden;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    List<Orden> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Orden> findAllByOrderByFechaCreacionDesc();

    List<Orden> findByEstadoPagoAndFechaCreacionBetween(
            EstadoPago estadoPago, Instant desde, Instant hasta);
}
