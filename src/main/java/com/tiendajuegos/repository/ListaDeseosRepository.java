package com.tiendajuegos.repository;

import com.tiendajuegos.domain.ListaDeseos;
import com.tiendajuegos.domain.ListaDeseosId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ListaDeseosRepository extends JpaRepository<ListaDeseos, ListaDeseosId> {

    @Query("select l from ListaDeseos l where l.id.usuarioId = :usuarioId")
    List<ListaDeseos> findByUsuario(@Param("usuarioId") Long usuarioId);
}
