package com.tiendajuegos.repository;

import com.tiendajuegos.domain.Carrito;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByUsuarioId(Long usuarioId);
}
