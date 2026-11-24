package com.tiendajuegos.social;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListaDeseosRepository extends JpaRepository<ListaDeseos, ListaDeseosId> {
    List<ListaDeseos> findByIdUsuarioId(Long usuarioId);
}
