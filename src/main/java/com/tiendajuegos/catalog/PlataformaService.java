package com.tiendajuegos.catalog;

import com.tiendajuegos.catalog.CatalogDtos.NombreRequest;
import com.tiendajuegos.catalog.CatalogDtos.NombreResponse;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.domain.Plataforma;
import com.tiendajuegos.repository.PlataformaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlataformaService {

    private final PlataformaRepository repository;

    public PlataformaService(PlataformaRepository repository) {
        this.repository = repository;
    }

    public List<NombreResponse> listar() {
        return repository.findAllByOrderByNombreAsc().stream()
                .map(plataforma -> new NombreResponse(plataforma.getId(), plataforma.getNombre()))
                .toList();
    }

    @Transactional
    public NombreResponse crear(NombreRequest request) {
        String nombre = request.nombre().trim();
        if (repository.existsByNombreIgnoreCase(nombre)) {
            throw ApiException.conflict("La plataforma ya existe");
        }
        Plataforma guardada = repository.save(new Plataforma(nombre));
        return new NombreResponse(guardada.getId(), guardada.getNombre());
    }
}
