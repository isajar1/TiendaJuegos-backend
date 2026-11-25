package com.tiendajuegos.sales;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUser;
import com.tiendajuegos.inventory.ClaveDigitalRepository;
import com.tiendajuegos.inventory.KeyCipherService;
import com.tiendajuegos.user.Usuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock private OrdenRepository ordenRepository;
    @Mock private DetalleOrdenRepository detalleRepository;
    @Mock private ClaveDigitalRepository claveRepository;
    @Mock private KeyCipherService cipher;
    @Mock private CurrentUser currentUser;

    @InjectMocks private OrdenService ordenService;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private Orden crearOrdenMock(Long idUsuario) {
        Usuario u = mock(Usuario.class);
        lenient().when(u.getId()).thenReturn(idUsuario);
        Orden orden = mock(Orden.class);
        lenient().when(orden.getId()).thenReturn(10L);
        lenient().when(orden.getUsuario()).thenReturn(u);
        lenient().when(orden.getTotal()).thenReturn(BigDecimal.TEN);
        lenient().when(orden.getEstadoPago()).thenReturn(EstadoPago.pagado);
        lenient().when(orden.getFechaCreacion()).thenReturn(LocalDateTime.now());
        return orden;
    }

    @Test
    void historialDelCliente_retornaOrdenesDelUsuarioActual() {
        when(currentUser.id()).thenReturn(5L);
        Orden orden = crearOrdenMock(5L);
        when(ordenRepository.findByUsuario(5L)).thenReturn(List.of(orden));
        when(detalleRepository.findByOrdenId(10L)).thenReturn(List.of());

        var resultado = ordenService.historialDelCliente();
        assertEquals(1, resultado.size());
    }

    @Test
    void todasLasOrdenes_retornaListaOrdenada() {
        Orden orden = crearOrdenMock(5L);
        when(ordenRepository.findAll(any(Sort.class))).thenReturn(List.of(orden));
        when(detalleRepository.findByOrdenId(10L)).thenReturn(List.of());

        var resultado = ordenService.todasLasOrdenes();
        assertEquals(1, resultado.size());
    }

    @Test
    void porId_comoClienteDueno_retornaOrden() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))));
        when(currentUser.id()).thenReturn(5L);
        Orden orden = crearOrdenMock(5L);
        when(ordenRepository.findById(10L)).thenReturn(Optional.of(orden));
        when(detalleRepository.findByOrdenId(10L)).thenReturn(List.of());

        assertNotNull(ordenService.porId(10L));
    }

    @Test
    void porId_comoClienteAjeno_lanzaForbidden() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))));
        when(currentUser.id()).thenReturn(99L);
        Orden orden = crearOrdenMock(5L);
        when(ordenRepository.findById(10L)).thenReturn(Optional.of(orden));

        ApiException ex = assertThrows(ApiException.class, () -> ordenService.porId(10L));
        assertEquals(HttpStatus.FORBIDDEN, ex.status());
    }

    @Test
    void porId_comoAdmin_permiteConsultarOrdenDeOtroCliente() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        Orden orden = crearOrdenMock(5L);
        when(ordenRepository.findById(10L)).thenReturn(Optional.of(orden));
        when(detalleRepository.findByOrdenId(10L)).thenReturn(List.of());

        assertNotNull(ordenService.porId(10L));
    }

    @Test
    void porId_noExiste_lanzaNotFound() {
        when(ordenRepository.findById(404L)).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () -> ordenService.porId(404L));
        assertEquals(HttpStatus.NOT_FOUND, ex.status());
    }
}