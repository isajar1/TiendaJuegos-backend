package com.tiendajuegos.auth;

import com.tiendajuegos.user.Role;
import com.tiendajuegos.user.Usuario;
import com.tiendajuegos.user.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        return crearUsuario(request, Role.cliente);
    }

    /** Alta de administradores: solo puede hacerlo una cuenta admin ya autenticada. */
    @Transactional
    public AuthResponse crearAdmin(RegisterRequest request, String correoAdministrador) {
        Usuario solicitante = usuarioRepository.findByCorreo(correoAdministrador)
                .orElseThrow(() -> new AccessDeniedException("Administrador no encontrado"));
        if (solicitante.getRol() != Role.admin || solicitante.isEliminado()) {
            throw new AccessDeniedException("Solo un administrador puede crear administradores");
        }
        return crearUsuario(request, Role.admin);
    }

    private AuthResponse crearUsuario(RegisterRequest request, Role rol) {
        String correo = normalizeEmail(request.correo());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new EmailAlreadyRegisteredException();
        }

        Usuario usuario = new Usuario(
                request.nombre().trim(),
                correo,
                passwordEncoder.encode(request.contrasena()),
                rol
        );
        Usuario savedUsuario = usuarioRepository.save(usuario);
        return responseFor(savedUsuario);
    }

    public AuthResponse login(LoginRequest request) {
        String correo = normalizeEmail(request.correo());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(correo, request.contrasena()));
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(InvalidCredentialsException::new);
        if (usuario.isEliminado()) {
            throw new InvalidCredentialsException();
        }
        return responseFor(usuario);
    }

    private AuthResponse responseFor(Usuario usuario) {
        UserDetails userDetails = User.withUsername(usuario.getCorreo())
                .password(usuario.getContrasenaHash())
                .authorities("ROLE_" + usuario.getRol().name().toUpperCase())
                .build();
        return new AuthResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol().name(),
                jwtService.generateToken(userDetails)
        );
    }

    private String normalizeEmail(String correo) {
        return correo.trim().toLowerCase();
    }
}
