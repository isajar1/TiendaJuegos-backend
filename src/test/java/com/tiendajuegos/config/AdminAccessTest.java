package com.tiendajuegos.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tiendajuegos.catalog.CategoriaController;
import com.tiendajuegos.catalog.CatalogDtos.JuegoRequest;
import com.tiendajuegos.catalog.JuegoController;
import com.tiendajuegos.catalog.PlataformaController;
import com.tiendajuegos.cupon.CuponController;
import com.tiendajuegos.deseos.DeseosController;
import com.tiendajuegos.orders.OrdenController;
import com.tiendajuegos.reportes.ReporteController;
import com.tiendajuegos.resena.ResenaController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

/** Verifica las reglas de acceso declaradas en los controllers. */
class AdminAccessTest {

    @Test
    void administracionRequiereRolAdmin() throws Exception {
        assertEquals("hasRole('ADMIN')", preAuthorize(JuegoController.class.getMethod(
                "crear", JuegoRequest.class, Authentication.class)));
        assertEquals("hasRole('ADMIN')", preAuthorize(JuegoController.class.getMethod(
                "actualizar", Long.class, JuegoRequest.class, Authentication.class)));
        assertEquals("hasRole('ADMIN')", preAuthorize(JuegoController.class.getMethod(
                "desactivar", Long.class, Authentication.class)));
        assertEquals("hasRole('ADMIN')", preAuthorize(JuegoController.class.getMethod(
                "contarClaves", Long.class)));
        assertEquals("hasRole('ADMIN')", preAuthorize(OrdenController.class.getMethod("todas")));
        assertEquals("hasRole('ADMIN')", preAuthorize(OrdenController.class.getMethod(
                "cambiarEstado", Long.class, com.tiendajuegos.orders.OrderDtos.EstadoRequest.class,
                Authentication.class)));
        assertEquals("hasRole('ADMIN')", CuponController.class.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasRole('ADMIN')", ReporteController.class.getAnnotation(PreAuthorize.class).value());
    }

    @Test
    void lecturaPublicaNoRequiereRol() throws Exception {
        assertNull(preAuthorize(JuegoController.class.getMethod(
                "listar", String.class, String.class, String.class, String.class)));
        assertNull(preAuthorize(JuegoController.class.getMethod("obtener", Long.class)));
        assertNull(preAuthorize(CategoriaController.class.getMethod("listar")));
        assertNull(preAuthorize(PlataformaController.class.getMethod("listar")));
        assertNull(preAuthorize(ResenaController.class.getMethod("porJuego", Long.class)));
    }

    @Test
    void operacionesDeClientePermitenClienteYAdmin() throws Exception {
        assertEquals("hasAnyRole('CLIENTE', 'ADMIN')",
                preAuthorize(DeseosController.class.getAnnotation(PreAuthorize.class)));
        assertEquals("hasAnyRole('CLIENTE', 'ADMIN')", preAuthorize(OrdenController.class.getMethod(
                "mias", Authentication.class)));
        assertEquals("hasAnyRole('CLIENTE', 'ADMIN')", preAuthorize(ResenaController.class.getMethod(
                "crear", Long.class, com.tiendajuegos.resena.ResenaDtos.ResenaRequest.class,
                Authentication.class)));
    }

    private String preAuthorize(java.lang.reflect.Method method) {
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        return annotation == null ? null : annotation.value();
    }

    private String preAuthorize(PreAuthorize annotation) {
        assertNotNull(annotation);
        return annotation.value();
    }
}
