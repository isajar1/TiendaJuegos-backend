package com.tiendajuegos.reportes;

import com.tiendajuegos.common.CurrentUserService;
import com.tiendajuegos.reportes.ReportesDtos.ReporteClienteResponse;
import com.tiendajuegos.reportes.ReportesDtos.ReporteVentaResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final ReporteService reporteService;
    private final CurrentUserService currentUserService;

    public ReporteController(ReporteService reporteService, CurrentUserService currentUserService) {
        this.reporteService = reporteService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/ventas-juegos")
    public List<ReporteVentaResponse> ventas(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            Authentication authentication) {
        return reporteService.ventasPorJuego(desde, hasta, currentUserService.id(authentication));
    }

    @GetMapping("/compras-clientes")
    public List<ReporteClienteResponse> clientes(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            Authentication authentication) {
        return reporteService.comprasPorCliente(desde, hasta, currentUserService.id(authentication));
    }
}
