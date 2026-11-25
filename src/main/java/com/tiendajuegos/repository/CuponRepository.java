package com.tiendajuegos.repository;

import com.tiendajuegos.domain.Cupon;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuponRepository extends JpaRepository<Cupon, Long> {

    List<Cupon> findAllByOrderByIdAsc();

    Optional<Cupon> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
