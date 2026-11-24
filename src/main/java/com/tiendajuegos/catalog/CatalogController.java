package com.tiendajuegos.catalog;

import com.tiendajuegos.dto.ApiDtos.CatalogoItem;
import com.tiendajuegos.dto.ApiDtos.JuegoRequest;
import com.tiendajuegos.dto.ApiDtos.JuegoResponse;
import com.tiendajuegos.dto.ApiDtos.NombreRequest;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/juegos")
    public List<JuegoResponse> listar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Long plataformaId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) BigDecimal minPrecio,
            @RequestParam(required = false) BigDecimal maxPrecio,
            @RequestParam(required = false) EstadoJuego estado) {
        return catalogService.listar(titulo, plataformaId, categoriaId, region, minPrecio, maxPrecio, estado);
    }

    @GetMapping("/juegos/{id}")
    public JuegoResponse detalle(@PathVariable Long id) {
        return catalogService.detalle(id);
    }

    @PostMapping("/juegos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JuegoResponse> crear(@RequestBody JuegoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.crear(request));
    }

    @PutMapping("/juegos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public JuegoResponse actualizar(@PathVariable Long id, @RequestBody JuegoRequest request) {
        return catalogService.actualizar(id, request);
    }

    @DeleteMapping("/juegos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deshabilitar(@PathVariable Long id) {
        catalogService.deshabilitar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categorias")
    public List<CatalogoItem> categorias() {
        return catalogService.categorias();
    }

    @PostMapping("/categorias")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CatalogoItem> crearCategoria(@RequestBody NombreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogService.crearCategoria(request == null ? null : request.nombre()));
    }

    @GetMapping("/plataformas")
    public List<CatalogoItem> plataformas() {
        return catalogService.plataformas();
    }

    @PostMapping("/plataformas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CatalogoItem> crearPlataforma(@RequestBody NombreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogService.crearPlataforma(request == null ? null : request.nombre()));
    }
}
