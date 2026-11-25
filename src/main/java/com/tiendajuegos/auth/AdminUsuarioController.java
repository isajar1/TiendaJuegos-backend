package com.tiendajuegos.auth;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Altas de administradores: requiere un admin autenticado, nunca registro público. */
@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    private final AuthService authService;

    public AdminUsuarioController(AuthService authService) {
        this.authService = authService;
    }

    /** No se devuelve el JWT del nuevo usuario: debe iniciar sesión por su cuenta. */
    public record AdminCreadoResponse(Long id, String nombre, String correo, String rol) {
    }

    @PostMapping("/administradores")
    public ResponseEntity<AdminCreadoResponse> crearAdministrador(
            @Valid @RequestBody RegisterRequest request, Authentication authentication) {
        AuthResponse creado = authService.crearAdmin(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AdminCreadoResponse(
                creado.id(), creado.nombre(), creado.correo(), creado.rol()));
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<Map<String, String>> correoDuplicado(EmailAlreadyRegisteredException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
    }
}
