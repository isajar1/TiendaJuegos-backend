package com.tiendajuegos.catalog;

import com.tiendajuegos.catalog.CatalogDtos.ClavesDisponiblesResponse;
import com.tiendajuegos.catalog.CatalogDtos.ClavesRequest;
import com.tiendajuegos.catalog.CatalogDtos.ClavesResponse;
import com.tiendajuegos.catalog.CatalogDtos.JuegoRequest;
import com.tiendajuegos.catalog.CatalogDtos.JuegoResponse;
import com.tiendajuegos.common.CurrentUserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/juegos")
public class JuegoController {

    private final JuegoService juegoService;
    private final CurrentUserService currentUserService;

    public JuegoController(JuegoService juegoService, CurrentUserService currentUserService) {
        this.juegoService = juegoService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<JuegoResponse> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String plataforma,
            @RequestParam(required = false) String region) {
        return juegoService.listar(q, categoria, plataforma, region);
    }

    @GetMapping("/{id}")
    public JuegoResponse obtener(@PathVariable Long id) {
        return juegoService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JuegoResponse> crear(@Valid @RequestBody JuegoRequest request,
                                               Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(juegoService.crear(request, currentUserService.id(authentication)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public JuegoResponse actualizar(@PathVariable Long id, @Valid @RequestBody JuegoRequest request,
                                    Authentication authentication) {
        return juegoService.actualizar(id, request, currentUserService.id(authentication));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id, Authentication authentication) {
        juegoService.desactivar(id, currentUserService.id(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/claves")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClavesResponse> agregarClaves(@PathVariable Long id,
                                                        @Valid @RequestBody ClavesRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(juegoService.agregarClaves(id, request, currentUserService.id(authentication)));
    }

    @GetMapping("/{id}/claves")
    @PreAuthorize("hasRole('ADMIN')")
    public ClavesDisponiblesResponse contarClaves(@PathVariable Long id) {
        return juegoService.contarClaves(id);
    }
}
