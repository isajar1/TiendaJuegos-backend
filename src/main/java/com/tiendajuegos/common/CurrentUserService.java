package com.tiendajuegos.common;

import com.tiendajuegos.user.Usuario;
import com.tiendajuegos.user.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/** Resuelve el usuario autenticado a partir del token JWT. */
@Service
public class CurrentUserService {

    private final UsuarioRepository usuarioRepository;

    public CurrentUserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Long id(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw ApiException.unauthorized("Autenticación requerida");
        }
        return usuarioRepository.findByCorreo(authentication.getName())
                .map(Usuario::getId)
                .orElseThrow(() -> ApiException.unauthorized("Usuario no encontrado"));
    }
}
