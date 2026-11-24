package com.tiendajuegos.audit;

import com.tiendajuegos.common.CurrentUser;
import com.tiendajuegos.dto.ApiDtos;
import com.tiendajuegos.user.Usuario;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registra las operaciones sensibles sin exponer datos como las claves digitales. */
@Service
public class AuditService {

    private final LogAuditoriaRepository repository;
    private final CurrentUser currentUser;

    public AuditService(LogAuditoriaRepository repository, CurrentUser currentUser) {
        this.repository = repository;
        this.currentUser = currentUser;
    }

    public void registrar(String accion, String entidad) {
        Usuario usuario = null;
        try {
            usuario = currentUser.usuario();
        } catch (RuntimeException ignored) {
            // Las acciones publicas tambien pueden registrarse sin usuario autenticado.
        }
        repository.save(new LogAuditoria(usuario, accion, entidad));
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.AuditoriaResponse> listar() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "fechaHora")).stream()
                .map(registro -> new ApiDtos.AuditoriaResponse(
                        registro.getId(),
                        registro.getUsuario() == null ? null : registro.getUsuario().getCorreo(),
                        registro.getAccion(),
                        registro.getEntidad(),
                        registro.getFechaHora()))
                .toList();
    }
}
