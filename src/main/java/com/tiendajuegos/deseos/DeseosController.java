package com.tiendajuegos.deseos;

import com.tiendajuegos.common.CurrentUserService;
import com.tiendajuegos.deseos.DeseosDtos.DeseoResponse;
import com.tiendajuegos.deseos.DeseosDtos.DeseosResult;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deseos")
@PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
public class DeseosController {

    private final DeseosService deseosService;
    private final CurrentUserService currentUserService;

    public DeseosController(DeseosService deseosService, CurrentUserService currentUserService) {
        this.deseosService = deseosService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<DeseoResponse> listar(Authentication authentication) {
        return deseosService.listar(currentUserService.id(authentication));
    }

    @PostMapping
    public ResponseEntity<List<DeseoResponse>> agregar(@Valid @RequestBody CartRequest request,
                                                       Authentication authentication) {
        DeseosResult resultado = deseosService.agregar(currentUserService.id(authentication), request.juegoId());
        HttpStatus status = resultado.creado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(resultado.deseos());
    }

    @DeleteMapping("/{juegoId}")
    public List<DeseoResponse> eliminar(@PathVariable Long juegoId, Authentication authentication) {
        return deseosService.eliminar(currentUserService.id(authentication), juegoId);
    }

    public record CartRequest(@jakarta.validation.constraints.NotNull Long juegoId) {
    }
}
