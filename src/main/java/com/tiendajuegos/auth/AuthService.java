package com.tiendajuegos.auth;

import com.tiendajuegos.user.Role;
import com.tiendajuegos.user.Usuario;
import com.tiendajuegos.user.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        String correo = normalizeEmail(request.correo());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new EmailAlreadyRegisteredException();
        }

        Usuario usuario = new Usuario(
                request.nombre().trim(),
                correo,
                passwordEncoder.encode(request.contrasena()),
                Role.cliente
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
