package com.tiendajuegos.cupon;

import com.tiendajuegos.common.CurrentUserService;
import com.tiendajuegos.cupon.CuponDtos.ActivoRequest;
import com.tiendajuegos.cupon.CuponDtos.CuponRequest;
import com.tiendajuegos.cupon.CuponDtos.CuponResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cupones")
@PreAuthorize("hasRole('ADMIN')")
public class CuponController {

    private final CuponService cuponService;
    private final CurrentUserService currentUserService;

    public CuponController(CuponService cuponService, CurrentUserService currentUserService) {
        this.cuponService = cuponService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<CuponResponse> listar() {
        return cuponService.listar();
    }

    @PostMapping
    public ResponseEntity<CuponResponse> crear(@Valid @RequestBody CuponRequest request,
                                               Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cuponService.crear(request, currentUserService.id(authentication)));
    }

    @PutMapping("/{id}")
    public CuponResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActivoRequest request,
                                    Authentication authentication) {
        return cuponService.actualizar(id, request, currentUserService.id(authentication));
    }
}
