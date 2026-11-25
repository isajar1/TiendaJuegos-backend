package com.tiendajuegos.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUser;
import com.tiendajuegos.dto.ApiDtos.PerfilRequest;
import com.tiendajuegos.dto.ApiDtos.PerfilResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CurrentUser currentUser;
    @Mock private AuditService auditService;

    @InjectMocks private PerfilService perfilService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = mock(Usuario.class);
        lenient().when(usuario.getId()).thenReturn(1L);
        lenient().when(usuario.getNombre()).thenReturn("Juan");
        lenient().when(usuario.getCorreo()).thenReturn("juan@test.com");
        lenient().when(usuario.getRol()).thenReturn(Role.cliente);
        lenient().when(usuario.getContrasenaHash()).thenReturn("HASH_ACTUAL");
        lenient().when(currentUser.usuario()).thenReturn(usuario);
    }

    @Test
    void yo_retornaDatosDelUsuarioActual() {
        PerfilResponse resp = perfilService.yo();
        assertEquals("Juan", resp.nombre());
        assertEquals("juan@test.com", resp.correo());
        assertEquals("cliente", resp.rol());
    }

    @Test
    void actualizar_nombreYContrasenaValidos_guardaYAudita() {
        when(passwordEncoder.matches("actual123", "HASH_ACTUAL")).thenReturn(true);
        when(passwordEncoder.encode("nueva1234")).thenReturn("HASH_NUEVO");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        PerfilResponse resp = perfilService.actualizar(new PerfilRequest("  Juan Carlos  ", "actual123", "nueva1234"));

        assertNotNull(resp);
        verify(usuario).setNombre("Juan Carlos");
        verify(usuario).setContrasenaHash("HASH_NUEVO");
        verify(auditService).registrar("EDITAR_PERFIL", "usuarios");
    }

    @Test
    void actualizar_erroresDeValidacion_lanzaBadRequest() {
        assertThrows(ApiException.class, () -> perfilService.actualizar(null));

        // Nombre demasiado largo (>120)
        String nombreLargo = "a".repeat(121);
        assertThrows(ApiException.class, () -> perfilService.actualizar(new PerfilRequest(nombreLargo, null, null)));

        // Contrasena actual incorrecta
        when(passwordEncoder.matches("mala", "HASH_ACTUAL")).thenReturn(false);
        assertThrows(ApiException.class, () -> perfilService.actualizar(new PerfilRequest(null, "mala", "nueva1234")));

        // Contrasena nueva muy corta (<8)
        when(passwordEncoder.matches("actual123", "HASH_ACTUAL")).thenReturn(true);
        assertThrows(ApiException.class, () -> perfilService.actualizar(new PerfilRequest(null, "actual123", "123")));
    }
}