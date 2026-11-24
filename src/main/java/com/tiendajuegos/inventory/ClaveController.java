package com.tiendajuegos.inventory;

import com.tiendajuegos.dto.ApiDtos.ClavesRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/juegos/{juegoId}/claves")
public class ClaveController {

    private final ClaveService claveService;

    public ClaveController(ClaveService claveService) {
        this.claveService = claveService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Integer>> cargar(@PathVariable Long juegoId,
                                                        @RequestBody ClavesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("clavesCargadas", claveService.cargar(juegoId, request)));
    }
}
