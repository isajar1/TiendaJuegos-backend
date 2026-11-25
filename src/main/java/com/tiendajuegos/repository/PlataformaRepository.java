package com.tiendajuegos.repository;

import com.tiendajuegos.domain.Plataforma;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlataformaRepository extends JpaRepository<Plataforma, Long> {

    List<Plataforma> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
