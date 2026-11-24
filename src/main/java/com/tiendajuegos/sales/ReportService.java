package com.tiendajuegos.sales;

import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.dto.ApiDtos.ReporteItemResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final List<EstadoPago> ESTADOS_CONTABILIZADOS =
            List.of(EstadoPago.pagado, EstadoPago.reembolsado);

    private final OrdenRepository ordenRepository;

    public ReportService(OrdenRepository ordenRepository) {
        this.ordenRepository = ordenRepository;
    }

    public List<ReporteItemResponse> ventasPorJuego(LocalDateTime desde, LocalDateTime hasta) {
        validarRango(desde, hasta);
        return ordenRepository.ventasPorJuego(ESTADOS_CONTABILIZADOS, desde, hasta).stream()
                .map(fila -> new ReporteItemResponse((String) fila[0], ((Number) fila[1]).longValue(),
                        new BigDecimal(fila[2].toString())))
                .toList();
    }

    public List<ReporteItemResponse> comprasPorCliente(LocalDateTime desde, LocalDateTime hasta) {
        validarRango(desde, hasta);
        return ordenRepository.comprasPorCliente(ESTADOS_CONTABILIZADOS, desde, hasta).stream()
                .map(fila -> new ReporteItemResponse((String) fila[0], ((Number) fila[1]).longValue(),
                        new BigDecimal(fila[2].toString())))
                .toList();
    }

    private void validarRango(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debes enviar las fechas inicial y final.");
        }
        if (desde.isAfter(hasta)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El rango de fechas no es valido.");
        }
    }
}
