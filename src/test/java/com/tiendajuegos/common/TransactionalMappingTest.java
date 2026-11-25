package com.tiendajuegos.common;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tiendajuegos.cart.CarritoService;
import com.tiendajuegos.catalog.JuegoService;
import com.tiendajuegos.cupon.CuponService;
import com.tiendajuegos.domain.Carrito;
import com.tiendajuegos.orders.OrdenService;
import com.tiendajuegos.reportes.ReporteService;
import com.tiendajuegos.resena.ResenaService;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

/** Evita que el mapeo de relaciones LAZY vuelva a romperse con open-in-view=false. */
class TransactionalMappingTest {

    private record Consulta(Class<?> servicio, String metodo, boolean readOnly, Class<?>... parametros) {
    }

    @Test
    void todasLasConsultasQueMapeanRelacionesLazySonTransaccionales() throws Exception {
        List<Consulta> consultas = List.of(
                new Consulta(JuegoService.class, "listar", true, String.class, String.class, String.class, String.class),
                new Consulta(JuegoService.class, "obtener", true, Long.class),
                new Consulta(JuegoService.class, "contarClaves", true, Long.class),
                new Consulta(CarritoService.class, "obtener", true, Long.class),
                new Consulta(CarritoService.class, "resumen", true, Carrito.class),
                new Consulta(ResenaService.class, "porJuego", true, Long.class),
                new Consulta(OrdenService.class, "mias", true, Long.class),
                new Consulta(OrdenService.class, "todas", true),
                new Consulta(OrdenService.class, "obtener", true, Long.class),
                new Consulta(OrdenService.class, "obtenerMia", true, Long.class, Long.class),
                new Consulta(CuponService.class, "validar", true, String.class),
                // Estos dos escriben en auditoría: readOnly=false es intencional.
                new Consulta(ReporteService.class, "ventasPorJuego", false, String.class, String.class, Long.class),
                new Consulta(ReporteService.class, "comprasPorCliente", false, String.class, String.class, Long.class));

        for (Consulta consulta : consultas) {
            Method metodo = consulta.servicio().getMethod(consulta.metodo(), consulta.parametros());
            Transactional anotacion = metodo.getAnnotation(Transactional.class);
            assertNotNull(anotacion, consulta.servicio().getSimpleName() + "." + consulta.metodo()
                    + " debe ser transaccional");
            assertTrue(anotacion.readOnly() == consulta.readOnly(), consulta.servicio().getSimpleName() + "."
                    + consulta.metodo() + " debe tener readOnly=" + consulta.readOnly());
        }
    }
}
