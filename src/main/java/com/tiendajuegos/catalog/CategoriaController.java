package com.tiendajuegos.catalog;

import com.tiendajuegos.catalog.CatalogDtos.NombreRequest;
import com.tiendajuegos.catalog.CatalogDtos.NombreResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<NombreResponse> listar() {
        return categoriaService.listar();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NombreResponse> crear(@Valid @RequestBody NombreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(request));
    }
}
