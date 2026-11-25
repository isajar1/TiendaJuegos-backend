package com.tiendajuegos.orders;

import com.tiendajuegos.common.CurrentUserService;
import com.tiendajuegos.orders.OrderDtos.EstadoRequest;
import com.tiendajuegos.orders.OrderDtos.OrdenResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService ordenService;
    private final CurrentUserService currentUserService;

    public OrdenController(OrdenService ordenService, CurrentUserService currentUserService) {
        this.ordenService = ordenService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/mias")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public List<OrdenResponse> mias(Authentication authentication) {
        return ordenService.mias(currentUserService.id(authentication));
    }

    @GetMapping("/mias/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public OrdenResponse mia(@PathVariable Long id, Authentication authentication) {
        return ordenService.obtenerMia(currentUserService.id(authentication), id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrdenResponse> todas() {
        return ordenService.todas();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public OrdenResponse obtener(@PathVariable Long id) {
        return ordenService.obtener(id);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public OrdenResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request,
                                       Authentication authentication) {
        return ordenService.cambiarEstado(id, request, currentUserService.id(authentication));
    }
}
