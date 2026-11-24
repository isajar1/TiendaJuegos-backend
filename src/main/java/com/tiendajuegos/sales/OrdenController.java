package com.tiendajuegos.sales;

import com.tiendajuegos.dto.ApiDtos.CheckoutRequest;
import com.tiendajuegos.dto.ApiDtos.OrdenResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final CheckoutService checkoutService;
    private final OrdenService ordenService;

    public OrdenController(CheckoutService checkoutService, OrdenService ordenService) {
        this.checkoutService = checkoutService;
        this.ordenService = ordenService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<OrdenResponse> pagar(@RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(checkoutService.pagar(request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<OrdenResponse> miHistorial() {
        return ordenService.historialDelCliente();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrdenResponse> todas() {
        return ordenService.todasLasOrdenes();
    }

    @GetMapping("/{id}")
    public OrdenResponse porId(@PathVariable Long id) {
        return ordenService.porId(id);
    }
}
