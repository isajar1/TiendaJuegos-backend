package com.tiendajuegos.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tiendajuegos.user.Role;
import com.tiendajuegos.user.Usuario;
import com.tiendajuegos.user.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(4);
        authService = new AuthService(usuarioRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void registerCreatesClientWithEncryptedPassword() {
        RegisterRequest request = new RegisterRequest("Sarai", "SARAI@MAIL.COM", "password123");
        when(usuarioRepository.existsByCorreo("sarai@mail.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertEquals("sarai@mail.com", response.correo());
        assertEquals("cliente", response.rol());
        assertEquals("jwt-token", response.token());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void registerRejectsExistingEmail() {
        when(usuarioRepository.existsByCorreo("sarai@mail.com")).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class,
                () -> authService.register(new RegisterRequest("Sarai", "sarai@mail.com", "password123")));
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        Usuario usuario = new Usuario("Sarai", "sarai@mail.com", passwordEncoder.encode("password123"), Role.cliente);
        when(usuarioRepository.findByCorreo("sarai@mail.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("sarai@mail.com", "password123"));

        assertEquals("jwt-token", response.token());
        assertEquals("sarai@mail.com", response.correo());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void loginRejectsInvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("sarai@mail.com", "bad-password")));
    }
}
