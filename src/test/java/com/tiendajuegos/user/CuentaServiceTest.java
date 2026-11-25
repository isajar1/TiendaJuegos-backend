package com.tiendajuegos.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tiendajuegos.TestFixtures;
import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.domain.CarritoItem;
import com.tiendajuegos.domain.ListaDeseos;
import com.tiendajuegos.repository.CarritoItemRepository;
import com.tiendajuegos.repository.CarritoRepository;
import com.tiendajuegos.repository.ListaDeseosRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CarritoRepository carritoRepository;
    @Mock private CarritoItemRepository itemRepository;
    @Mock private ListaDeseosRepository listaDeseosRepository;
    @Mock private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;

    private CuentaService service() {
        return new CuentaService(usuarioRepository, carritoRepository, itemRepository,
                listaDeseosRepository, passwordEncoder, auditService);
    }

    @Test
    void eliminarMiCuentaAnonimizaYConservaElHistorial() {
        Usuario usuario = TestFixtures.usuario(7L, "Ana", "ana@mail.com", Role.cliente);
        var carrito = TestFixtures.carrito(1L, usuario, null);
        List<CarritoItem> items = List.of(TestFixtures.item(1L, carrito, TestFixtures.juego(1L, "Juego", java.math.BigDecimal.TEN)));
        List<ListaDeseos> deseos = List.of(TestFixtures.deseo(7L, 1L));

        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(7L)).thenReturn(Optional.of(carrito));
        when(itemRepository.findByCarritoIdOrderByIdAsc(1L)).thenReturn(items);
        when(listaDeseosRepository.findByUsuario(7L)).thenReturn(deseos);
        when(passwordEncoder.encode(anyString())).thenReturn("hash-aleatorio");

        service().eliminarMiCuenta(7L);

        assertTrue(usuario.isEliminado());
        assertEquals("Usuario eliminado", usuario.getNombre());
        assertEquals("eliminado-7@invalid.local", usuario.getCorreo());
        assertEquals("hash-aleatorio", usuario.getContrasenaHash());
        assertNotEquals("hash", usuario.getContrasenaHash());
        assertNotNull(usuario.getFechaEliminacion());
        verify(itemRepository).deleteAll(items);
        verify(carritoRepository).delete(carrito);
        verify(listaDeseosRepository).deleteAll(deseos);
        verify(usuarioRepository).save(usuario);
        verify(auditService).registrar(7L, "cuenta eliminada", "usuario");
    }

    @Test
    void eliminarMiCuentaEsIdempotente() {
        Usuario usuario = TestFixtures.usuario(7L, "Ana", "ana@mail.com", Role.cliente);
        usuario.anonimizar("hash-aleatorio");
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));

        service().eliminarMiCuenta(7L);

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(carritoRepository, itemRepository, listaDeseosRepository, auditService);
        assertNotNull(usuario.getFechaEliminacion());
    }

    @Test
    void eliminarMiCuentaInexistenteLanza404() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException fallo = assertThrows(ApiException.class, () -> service().eliminarMiCuenta(99L));

        assertEquals(HttpStatus.NOT_FOUND, fallo.getStatus());
        verifyNoInteractions(carritoRepository, itemRepository, listaDeseosRepository, auditService);
    }
}
