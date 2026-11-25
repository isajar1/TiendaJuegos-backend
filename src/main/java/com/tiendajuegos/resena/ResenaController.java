package com.tiendajuegos.resena;

import com.tiendajuegos.common.CurrentUserService;
import com.tiendajuegos.resena.ResenaDtos.ResenaRequest;
import com.tiendajuegos.resena.ResenaDtos.ResenaResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResenaController {

    private final ResenaService resenaService;
    private final CurrentUserService currentUserService;

    public ResenaController(ResenaService resenaService, CurrentUserService currentUserService) {
        this.resenaService = resenaService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/api/juegos/{id}/resenas")
    public List<ResenaResponse> porJuego(@PathVariable Long id) {
        return resenaService.porJuego(id);
    }

    @PostMapping("/api/juegos/{id}/resenas")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<ResenaResponse> crear(@PathVariable Long id,
                                                @Valid @RequestBody ResenaRequest request,
                                                Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resenaService.crear(id, currentUserService.id(authentication), request));
    }

    @DeleteMapping("/api/resenas/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        Long usuarioId = currentUserService.id(authentication);
        boolean administrador = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(autoridad -> "ROLE_ADMIN".equals(autoridad.getAuthority()));
        resenaService.eliminar(id, usuarioId, administrador);
        return ResponseEntity.noContent().build();
    }
}
