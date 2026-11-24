package com.tiendajuegos.social;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.catalog.Juego;
import com.tiendajuegos.catalog.JuegoRepository;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUser;
import com.tiendajuegos.dto.ApiDtos.ResenaRequest;
import com.tiendajuegos.dto.ApiDtos.ResenaResponse;
import com.tiendajuegos.sales.DetalleOrdenRepository;
import com.tiendajuegos.sales.EstadoPago;
import com.tiendajuegos.user.Usuario;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final JuegoRepository juegoRepository;
    private final DetalleOrdenRepository detalleRepository;
    private final CurrentUser currentUser;
    private final AuditService auditService;

    public ResenaService(ResenaRepository resenaRepository,
                         JuegoRepository juegoRepository,
                         DetalleOrdenRepository detalleRepository,
                         CurrentUser currentUser,
                         AuditService auditService) {
        this.resenaRepository = resenaRepository;
        this.juegoRepository = juegoRepository;
        this.detalleRepository = detalleRepository;
        this.currentUser = currentUser;
        this.auditService = auditService;
    }

    public List<ResenaResponse> listar(Long juegoId) {
        if (!juegoRepository.existsById(juegoId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Juego no encontrado.");
        }
        return resenaRepository.findByJuegoId(juegoId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ResenaResponse crear(Long juegoId, ResenaRequest request) {
        if (request == null || request.calificacion() < 1 || request.calificacion() > 5) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La calificacion debe estar entre 1 y 5.");
        }
        Juego juego = juegoRepository.findById(juegoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Juego no encontrado."));
        Long usuarioId = currentUser.id();
        if (!detalleRepository.existsByOrdenUsuarioIdAndOrdenEstadoPagoAndJuegoId(
                usuarioId, EstadoPago.pagado, juegoId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo pueden calificar quienes compraron el juego.");
        }
        if (resenaRepository.findByJuegoIdAndUsuarioId(juegoId, usuarioId).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya publicaste una reseña para este juego.");
        }
        Usuario usuario = currentUser.usuario();
        Resena guardada = resenaRepository.save(
                new Resena(juego, usuario, request.calificacion(), request.comentario()));
        auditService.registrar("CREAR_RESENA", "resenas");
        return toResponse(guardada);
    }

    private ResenaResponse toResponse(Resena resena) {
        return new ResenaResponse(resena.getId(), resena.getJuego().getId(),
                resena.getUsuario().getCorreo(), resena.getCalificacion(),
                resena.getComentario(), resena.getFecha());
    }
}
