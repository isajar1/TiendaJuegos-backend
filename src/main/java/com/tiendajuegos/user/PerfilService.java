package com.tiendajuegos.user;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUser;
import com.tiendajuegos.dto.ApiDtos.PerfilRequest;
import com.tiendajuegos.dto.ApiDtos.PerfilResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;
    private final AuditService auditService;

    public PerfilService(UsuarioRepository usuarioRepository,
                         PasswordEncoder passwordEncoder,
                         CurrentUser currentUser,
                         AuditService auditService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUser = currentUser;
        this.auditService = auditService;
    }

    public PerfilResponse yo() {
        return toResponse(currentUser.usuario());
    }

    @Transactional
    public PerfilResponse actualizar(PerfilRequest request) {
        Usuario usuario = currentUser.usuario();
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La peticion no es valida.");
        }
        if (request.nombre() != null && !request.nombre().isBlank()) {
            String nombre = request.nombre().trim();
            if (nombre.length() > 120) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "El nombre es demasiado largo.");
            }
            usuario.setNombre(nombre);
        }
        if (request.contrasenaNueva() != null && !request.contrasenaNueva().isBlank()) {
            if (request.contrasenaActual() == null
                    || !passwordEncoder.matches(request.contrasenaActual(), usuario.getContrasenaHash())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta.");
            }
            String nueva = request.contrasenaNueva();
            if (nueva.length() < 8 || nueva.length() > 72) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña debe tener entre 8 y 72 caracteres.");
            }
            usuario.setContrasenaHash(passwordEncoder.encode(nueva));
        }
        Usuario guardado = usuarioRepository.save(usuario);
        auditService.registrar("EDITAR_PERFIL", "usuarios");
        return toResponse(guardado);
    }

    private PerfilResponse toResponse(Usuario usuario) {
        return new PerfilResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(),
                usuario.getRol().name());
    }
}
