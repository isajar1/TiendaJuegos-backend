package com.tiendajuegos.cupon;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.cupon.CuponDtos.ActivoRequest;
import com.tiendajuegos.cupon.CuponDtos.CuponRequest;
import com.tiendajuegos.cupon.CuponDtos.CuponResponse;
import com.tiendajuegos.domain.Cupon;
import com.tiendajuegos.repository.CuponRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuponService {

    private final CuponRepository repository;
    private final AuditService auditService;

    public CuponService(CuponRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    public List<CuponResponse> listar() {
        return repository.findAllByOrderByIdAsc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public CuponResponse crear(CuponRequest request, Long usuarioId) {
        String codigo = request.codigo().trim().toUpperCase();
        if (repository.existsByCodigo(codigo)) {
            throw ApiException.conflict("El cupón ya existe");
        }
        Cupon cupon = new Cupon(codigo, request.porcentajeDescuento(),
                parsearFecha(request.fechaExpiracion()), request.activo() == null || request.activo());
        Cupon guardado = repository.save(cupon);
        auditService.registrar(usuarioId, "cupón creado", "cupon");
        return toResponse(guardado);
    }

    @Transactional
    public CuponResponse actualizar(Long id, ActivoRequest request, Long usuarioId) {
        Cupon cupon = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Cupón no encontrado"));
        cupon.setActivo(request.activo());
        Cupon guardado = repository.save(cupon);
        auditService.registrar(usuarioId, "cupón actualizado", "cupon");
        return toResponse(guardado);
    }

    private LocalDateTime parsearFecha(String fecha) {
        try {
            return LocalDateTime.parse(fecha.trim());
        } catch (DateTimeParseException exception) {
            // También aceptamos el formato de fecha simple del contrato (yyyy-MM-dd).
            try {
                return java.time.LocalDate.parse(fecha.trim()).atTime(23, 59, 59);
            } catch (DateTimeParseException nested) {
                throw ApiException.badRequest("Fecha de expiración inválida");
            }
        }
    }

    private CuponResponse toResponse(Cupon cupon) {
        return new CuponResponse(cupon.getId(), cupon.getCodigo(), cupon.getPorcentajeDescuento(),
                cupon.getFechaExpiracion().toString(), cupon.isActivo());
    }

    /** Para el carrito: valida existencia, activación y vigencia. */
    @Transactional(readOnly = true)
    public Cupon validar(String codigo) {
        String normalizado = codigo.trim().toUpperCase();
        Cupon cupon = repository.findByCodigo(normalizado)
                .orElseThrow(() -> ApiException.badRequest("Cupón no encontrado"));
        if (!cupon.isVigente()) {
            throw ApiException.badRequest("El cupón no está vigente");
        }
        return cupon;
    }

    /** Expuesto para pruebas y para el cálculo del carrito. */
    public BigDecimal porcentaje(Cupon cupon) {
        return cupon.getPorcentajeDescuento();
    }
}
