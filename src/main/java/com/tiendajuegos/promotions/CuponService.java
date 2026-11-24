package com.tiendajuegos.promotions;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.dto.ApiDtos.CuponRequest;
import com.tiendajuegos.dto.ApiDtos.CuponResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CuponService {

    private final CuponRepository repository;
    private final AuditService auditService;

    public CuponService(CuponRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    public Cupon validar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ingresa un codigo de cupon.");
        }
        Cupon cupon = repository.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El cupon no existe."));
        if (!cupon.isActivo()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El cupon esta desactivado.");
        }
        if (cupon.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El cupon esta vencido.");
        }
        return cupon;
    }

    public BigDecimal descuento(BigDecimal subtotal, Cupon cupon) {
        return subtotal.multiply(cupon.getPorcentajeDescuento())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public List<CuponResponse> listar() {
        return repository.findAll(Sort.by("codigo")).stream().map(this::toResponse).toList();
    }

    @Transactional
    public CuponResponse crear(CuponRequest request) {
        if (request == null || request.codigo() == null || request.codigo().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El codigo del cupon es obligatorio.");
        }
        if (request.porcentajeDescuento() == null
                || request.porcentajeDescuento().compareTo(BigDecimal.ZERO) <= 0
                || request.porcentajeDescuento().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El descuento debe estar entre 0 y 100.");
        }
        if (request.fechaExpiracion() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fecha de expiracion es obligatoria.");
        }
        String codigo = request.codigo().trim();
        if (repository.existsByCodigoIgnoreCase(codigo)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un cupon con ese codigo.");
        }
        boolean activo = request.activo() == null || request.activo();
        Cupon guardado = repository.save(new Cupon(codigo, request.porcentajeDescuento(),
                request.fechaExpiracion(), activo));
        auditService.registrar("CREAR_CUPON", "cupones");
        return toResponse(guardado);
    }

    @Transactional
    public void desactivar(Long id) {
        Cupon cupon = repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cupon no encontrado."));
        cupon.setActivo(false);
        repository.save(cupon);
        auditService.registrar("DESACTIVAR_CUPON", "cupones");
    }

    private CuponResponse toResponse(Cupon cupon) {
        return new CuponResponse(cupon.getId(), cupon.getCodigo(), cupon.getPorcentajeDescuento(),
                cupon.getFechaExpiracion(), cupon.isActivo());
    }
}
