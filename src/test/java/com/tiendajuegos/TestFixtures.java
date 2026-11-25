package com.tiendajuegos;

import com.tiendajuegos.domain.Carrito;
import com.tiendajuegos.domain.CarritoItem;
import com.tiendajuegos.domain.Categoria;
import com.tiendajuegos.domain.ClaveDigital;
import com.tiendajuegos.domain.Cupon;
import com.tiendajuegos.domain.DetalleOrden;
import com.tiendajuegos.domain.EstadoClave;
import com.tiendajuegos.domain.EstadoJuego;
import com.tiendajuegos.domain.EstadoPago;
import com.tiendajuegos.domain.Juego;
import com.tiendajuegos.domain.ListaDeseos;
import com.tiendajuegos.domain.Orden;
import com.tiendajuegos.domain.Plataforma;
import com.tiendajuegos.domain.Resena;
import com.tiendajuegos.user.Role;
import com.tiendajuegos.user.Usuario;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

/** Constructores de entidades con id para pruebas unitarias (sin base de datos). */
public final class TestFixtures {

    private TestFixtures() {
    }

    public static Plataforma plataforma(long id, String nombre) {
        Plataforma plataforma = new Plataforma(nombre);
        ReflectionTestUtils.setField(plataforma, "id", id);
        return plataforma;
    }

    public static Categoria categoria(long id, String nombre) {
        Categoria categoria = new Categoria(nombre);
        ReflectionTestUtils.setField(categoria, "id", id);
        return categoria;
    }

    public static Juego juego(long id, String titulo, BigDecimal precio) {
        Juego juego = new Juego(titulo, "Descripción", precio, "Global",
                plataforma(1, "Steam"), categoria(1, "Accion"));
        ReflectionTestUtils.setField(juego, "id", id);
        return juego;
    }

    public static Juego inactivo(long id, String titulo, BigDecimal precio) {
        Juego juego = juego(id, titulo, precio);
        juego.setEstado(EstadoJuego.inactivo);
        return juego;
    }

    public static Usuario usuario(long id, String nombre, String correo, Role rol) {
        Usuario usuario = new Usuario(nombre, correo, "hash", rol);
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    public static Cupon cupon(long id, String codigo, BigDecimal porcentaje, boolean activo) {
        Cupon cupon = new Cupon(codigo, porcentaje, LocalDateTime.now().plusDays(30), activo);
        ReflectionTestUtils.setField(cupon, "id", id);
        return cupon;
    }

    public static Carrito carrito(long id, Usuario usuario, Cupon cupon) {
        Carrito carrito = new Carrito(usuario);
        carrito.setCupon(cupon);
        ReflectionTestUtils.setField(carrito, "id", id);
        return carrito;
    }

    public static CarritoItem item(long id, Carrito carrito, Juego juego) {
        CarritoItem item = new CarritoItem(carrito, juego);
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    public static ClaveDigital clave(long id, Juego juego, String cifrada) {
        ClaveDigital clave = new ClaveDigital(juego, cifrada);
        ReflectionTestUtils.setField(clave, "id", id);
        return clave;
    }

    public static Orden orden(long id, Usuario usuario, Cupon cupon, BigDecimal total, EstadoPago estado) {
        Orden orden = new Orden(usuario, cupon, total, estado);
        ReflectionTestUtils.setField(orden, "id", id);
        ReflectionTestUtils.setField(orden, "fechaCreacion", Instant.parse("2026-01-15T10:00:00Z"));
        return orden;
    }

    public static DetalleOrden detalle(long id, Orden orden, Juego juego, BigDecimal precio, ClaveDigital clave) {
        DetalleOrden detalle = new DetalleOrden(orden, juego, precio, clave);
        ReflectionTestUtils.setField(detalle, "id", id);
        return detalle;
    }

    public static Resena resena(long id, Juego juego, Usuario usuario, int calificacion, String comentario) {
        Resena resena = new Resena(juego, usuario, calificacion, comentario);
        ReflectionTestUtils.setField(resena, "id", id);
        ReflectionTestUtils.setField(resena, "fecha", Instant.parse("2026-01-15T10:00:00Z"));
        return resena;
    }

    public static ListaDeseos deseo(long usuarioId, long juegoId) {
        return new ListaDeseos(usuarioId, juegoId);
    }

    public static Authentication auth(String correo, String rol) {
        return new UsernamePasswordAuthenticationToken(correo, "token",
                List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
    }
}
