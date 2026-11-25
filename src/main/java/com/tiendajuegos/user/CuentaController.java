package com.tiendajuegos.user;

import com.tiendajuegos.common.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class CuentaController {

    private final CuentaService cuentaService;
    private final CurrentUserService currentUserService;

    public CuentaController(CuentaService cuentaService, CurrentUserService currentUserService) {
        this.cuentaService = cuentaService;
        this.currentUserService = currentUserService;
    }

    @DeleteMapping("/mi-cuenta")
    public ResponseEntity<Void> eliminarMiCuenta(Authentication authentication) {
        cuentaService.eliminarMiCuenta(currentUserService.id(authentication));
        return ResponseEntity.noContent().build();
    }
}
