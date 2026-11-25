package com.tiendajuegos.cart;

import com.tiendajuegos.cart.CartDtos.CarritoResponse;
import com.tiendajuegos.common.CurrentUserService;
import com.tiendajuegos.cupon.CuponDtos.CodigoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carritoService;
    private final CurrentUserService currentUserService;

    public CarritoController(CarritoService carritoService, CurrentUserService currentUserService) {
        this.carritoService = carritoService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public CarritoResponse obtener(Authentication authentication) {
        return carritoService.obtener(currentUserService.id(authentication));
    }

    @PostMapping("/items")
    public CarritoResponse agregar(@Valid @RequestBody CompraRequest request, Authentication authentication) {
        return carritoService.agregar(currentUserService.id(authentication), request.juegoId());
    }

    @DeleteMapping("/items/{juegoId}")
    public CarritoResponse eliminarItem(@PathVariable Long juegoId, Authentication authentication) {
        return carritoService.eliminarItem(currentUserService.id(authentication), juegoId);
    }

    @DeleteMapping
    public ResponseEntity<Void> vaciar(Authentication authentication) {
        carritoService.vaciar(currentUserService.id(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cupon")
    public CarritoResponse aplicarCupon(@Valid @RequestBody CodigoRequest request,
                                        Authentication authentication) {
        return carritoService.aplicarCupon(currentUserService.id(authentication), request);
    }

    /** Body compartido por las operaciones sobre items del carrito y la wishlist. */
    public record CompraRequest(@jakarta.validation.constraints.NotNull Long juegoId) {
    }
}
