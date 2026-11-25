package com.tiendajuegos.catalog;

import com.tiendajuegos.catalog.CatalogDtos.NombreRequest;
import com.tiendajuegos.catalog.CatalogDtos.NombreResponse;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.domain.Categoria;
import com.tiendajuegos.repository.CategoriaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<NombreResponse> listar() {
        return repository.findAllByOrderByNombreAsc().stream()
                .map(categoria -> new NombreResponse(categoria.getId(), categoria.getNombre()))
                .toList();
    }

    @Transactional
    public NombreResponse crear(NombreRequest request) {
        String nombre = request.nombre().trim();
        if (repository.existsByNombreIgnoreCase(nombre)) {
            throw ApiException.conflict("La categoría ya existe");
        }
        Categoria guardada = repository.save(new Categoria(nombre));
        return new NombreResponse(guardada.getId(), guardada.getNombre());
    }
}
