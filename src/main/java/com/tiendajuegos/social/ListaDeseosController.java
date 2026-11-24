package com.tiendajuegos.social;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.catalog.Juego;
import com.tiendajuegos.catalog.JuegoRepository;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUser;
import com.tiendajuegos.dto.ApiDtos.CatalogoItem;
import com.tiendajuegos.user.Usuario;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deseos")
public class ListaDeseosController {

    private final ListaDeseosRepository repository;
    private final JuegoRepository juegoRepository;
    private final CurrentUser currentUser;
    private final AuditService auditService;

    public ListaDeseosController(ListaDeseosRepository repository,
                                 JuegoRepository juegoRepository,
                                 CurrentUser currentUser,
                                 AuditService auditService) {
        this.repository = repository;
        this.juegoRepository = juegoRepository;
        this.currentUser = currentUser;
        this.auditService = auditService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<CatalogoItem> listar() {
        return repository.findByIdUsuarioId(currentUser.id()).stream()
                .map(deseo -> new CatalogoItem(deseo.getJuego().getId(), deseo.getJuego().getTitulo()))
                .toList();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<CatalogoItem> agregar(@RequestBody Map<String, Long> body) {
        Long juegoId = body == null ? null : body.get("juegoId");
        Juego juego = juegoRepository.findById(juegoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Juego no encontrado."));
        Usuario usuario = currentUser.usuario();
        ListaDeseosId id = new ListaDeseosId(usuario.getId(), juego.getId());
        if (repository.existsById(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "El juego ya esta en tu lista de deseos.");
        }
        repository.save(new ListaDeseos(usuario, juego));
        auditService.registrar("AGREGAR_DESEO", "listas_deseos");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CatalogoItem(juego.getId(), juego.getTitulo()));
    }

    @DeleteMapping("/{juegoId}")
    @Transactional
    public ResponseEntity<Void> quitar(@PathVariable Long juegoId) {
        ListaDeseosId id = new ListaDeseosId(currentUser.id(), juegoId);
        if (!repository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "El juego no esta en tu lista de deseos.");
        }
        repository.deleteById(id);
        auditService.registrar("QUITAR_DESEO", "listas_deseos");
        return ResponseEntity.noContent().build();
    }
}
