package com.tiendajuegos.resena;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.domain.EstadoPago;
import com.tiendajuegos.domain.Juego;
import com.tiendajuegos.domain.Resena;
import com.tiendajuegos.repository.DetalleOrdenRepository;
import com.tiendajuegos.repository.JuegoRepository;
import com.tiendajuegos.repository.ResenaRepository;
import com.tiendajuegos.resena.ResenaDtos.ResenaRequest;
import com.tiendajuegos.resena.ResenaDtos.ResenaResponse;
import com.tiendajuegos.user.Usuario;
import com.tiendajuegos.user.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final JuegoRepository juegoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DetalleOrdenRepository detalleRepository;
    private final AuditService auditService;

    public ResenaService(ResenaRepository resenaRepository, JuegoRepository juegoRepository,
                         UsuarioRepository usuarioRepository, DetalleOrdenRepository detalleRepository,
                         AuditService auditService) {
        this.resenaRepository = resenaRepository;
        this.juegoRepository = juegoRepository;
        this.usuarioRepository = usuarioRepository;
        this.detalleRepository = detalleRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ResenaResponse> porJuego(Long juegoId) {
        return resenaRepository.findByJuegoIdOrderByFechaDesc(juegoId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ResenaResponse crear(Long juegoId, Long usuarioId, ResenaRequest request) {
        Juego juego = juegoRepository.findById(juegoId)
                .orElseThrow(() -> ApiException.notFound("Juego no encontrado"));
        Integer calificacion = request.calificacion();
        if (calificacion == null || calificacion < 1 || calificacion > 5) {
            throw ApiException.badRequest("La calificación debe estar entre 1 y 5");
        }
        if (!detalleRepository.existsPaidPurchase(usuarioId, juegoId, EstadoPago.pagado)) {
            throw ApiException.conflict("Debes comprar este juego para poder reseñarlo");
        }
        if (resenaRepository.existsByUsuarioIdAndJuegoId(usuarioId, juegoId)) {
            throw ApiException.conflict("Ya reseñaste este juego");
        }
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        Resena resena = new Resena(juego, usuario, calificacion, request.comentario());
        Resena guardada = resenaRepository.save(resena);
        auditService.registrar(usuarioId, "reseña creada", "resena");
        return toResponse(guardada);
    }

    @Transactional
    public void eliminar(Long id, Long usuarioId, boolean administrador) {
        Resena resena = resenaRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Reseña no encontrada"));
        if (!administrador && !resena.getUsuario().getId().equals(usuarioId)) {
            throw ApiException.forbidden("No puedes eliminar la reseña de otro usuario");
        }
        resenaRepository.delete(resena);
        auditService.registrar(usuarioId, "reseña eliminada", "resena");
    }

    private ResenaResponse toResponse(Resena resena) {
        return new ResenaResponse(resena.getId(), resena.getJuego().getId(), resena.getUsuario().getId(),
                resena.getUsuario().getNombre(), resena.getCalificacion(), resena.getComentario(),
                resena.getFecha().toString());
    }
}
