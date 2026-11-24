package com.tiendajuegos.common;

import com.tiendajuegos.user.Usuario;
import com.tiendajuegos.user.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Acceso al usuario autenticado en la peticion actual. */
@Component
public class CurrentUser {

    private final UsuarioRepository usuarioRepository;

    public CurrentUser(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String correo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesion para realizar esta operacion.");
        }
        return authentication.getName();
    }

    public Usuario usuario() {
        String correo = correo();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "La sesion no es valida."));
    }

    public Long id() {
        return usuario().getId();
    }
}
