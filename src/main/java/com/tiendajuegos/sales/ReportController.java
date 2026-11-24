package com.tiendajuegos.sales;

import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.dto.ApiDtos.ReporteItemResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/ventas-por-juego")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReporteItemResponse> ventasPorJuego(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        return reportService.ventasPorJuego(fecha(desde), fecha(hasta));
    }

    @GetMapping("/compras-por-cliente")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReporteItemResponse> comprasPorCliente(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {
        return reportService.comprasPorCliente(fecha(desde), fecha(hasta));
    }

    private LocalDateTime fecha(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(valor.trim());
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(valor.trim()).atStartOfDay();
            } catch (DateTimeParseException exception) {
                throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Formato de fecha invalido: usa AAAA-MM-DD o AAAA-MM-DDTHH:mm:ss.");
            }
        }
    }
}
