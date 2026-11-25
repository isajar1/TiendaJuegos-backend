package com.tiendajuegos.deseos;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.deseos.DeseosDtos.DeseoResponse;
import com.tiendajuegos.deseos.DeseosDtos.DeseosResult;
import com.tiendajuegos.domain.Juego;
import com.tiendajuegos.domain.ListaDeseos;
import com.tiendajuegos.domain.ListaDeseosId;
import com.tiendajuegos.repository.JuegoRepository;
import com.tiendajuegos.repository.ListaDeseosRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeseosService {

    private final ListaDeseosRepository listaRepository;
    private final JuegoRepository juegoRepository;
    private final AuditService auditService;

    public DeseosService(ListaDeseosRepository listaRepository, JuegoRepository juegoRepository,
                         AuditService auditService) {
        this.listaRepository = listaRepository;
        this.juegoRepository = juegoRepository;
        this.auditService = auditService;
    }

    public List<DeseoResponse> listar(Long usuarioId) {
        List<Long> juegoIds = listaRepository.findByUsuario(usuarioId).stream()
                .map(deseo -> deseo.getId().getJuegoId())
                .toList();
        if (juegoIds.isEmpty()) {
            return List.of();
        }
        Map<Long, Juego> juegos = juegoRepository.findAllById(juegoIds).stream()
                .collect(Collectors.toMap(Juego::getId, Function.identity()));
        return juegoIds.stream()
                .map(juegos::get)
                .filter(java.util.Objects::nonNull)
                .map(juego -> new DeseoResponse(juego.getId(), juego.getTitulo(), juego.getPrecio()))
                .toList();
    }

    @Transactional
    public DeseosResult agregar(Long usuarioId, Long juegoId) {
        Juego juego = juegoRepository.findById(juegoId)
                .orElseThrow(() -> ApiException.notFound("Juego no encontrado"));
        ListaDeseosId id = new ListaDeseosId(usuarioId, juegoId);
        boolean creado = listaRepository.findById(id).isEmpty();
        if (creado) {
            listaRepository.save(new ListaDeseos(usuarioId, juegoId));
            auditService.registrar(usuarioId, "juego agregado a deseos", "lista_deseos");
        }
        return new DeseosResult(creado, listar(usuarioId));
    }

    @Transactional
    public List<DeseoResponse> eliminar(Long usuarioId, Long juegoId) {
        ListaDeseosId id = new ListaDeseosId(usuarioId, juegoId);
        listaRepository.findById(id).ifPresent(deseo -> {
            listaRepository.delete(deseo);
            auditService.registrar(usuarioId, "juego eliminado de deseos", "lista_deseos");
        });
        return listar(usuarioId);
    }
}
