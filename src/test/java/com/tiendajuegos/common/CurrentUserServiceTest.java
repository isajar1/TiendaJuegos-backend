package com.tiendajuegos.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.user.Role;
import com.tiendajuegos.user.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    @Test
    void resolvesIdFromAuthenticatedEmail() {
        when(usuarioRepository.findByCorreo("cliente@mail.com"))
                .thenReturn(Optional.of(TestFixtures.usuario(5L, "Cliente", "cliente@mail.com", Role.cliente)));

        assertEquals(5L, currentUserService.id(TestFixtures.auth("cliente@mail.com", "CLIENTE")));
    }

    @Test
    void rejectsMissingAuthentication() {
        ApiException exception = assertThrows(ApiException.class, () -> currentUserService.id(null));
        assertEquals(401, exception.getStatus().value());
    }

    @Test
    void rejectsUnknownUser() {
        when(usuarioRepository.findByCorreo("nobody@mail.com")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> currentUserService.id(TestFixtures.auth("nobody@mail.com", "CLIENTE")));
        assertEquals(401, exception.getStatus().value());
    }
}
