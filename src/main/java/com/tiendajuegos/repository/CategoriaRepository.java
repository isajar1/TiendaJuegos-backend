package com.tiendajuegos.repository;

import com.tiendajuegos.domain.Categoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
