package com.tiendajuegos.sales;

import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.common.CurrentUser;
import com.tiendajuegos.dto.ApiDtos.OrdenResponse;
import com.tiendajuegos.inventory.ClaveDigitalRepository;
import com.tiendajuegos.inventory.KeyCipherService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final DetalleOrdenRepository detalleRepository;
    private final ClaveDigitalRepository claveRepository;
    private final KeyCipherService cipher;
    private final CurrentUser currentUser;

    public OrdenService(OrdenRepository ordenRepository,
                        DetalleOrdenRepository detalleRepository,
                        ClaveDigitalRepository claveRepository,
                        KeyCipherService cipher,
                        CurrentUser currentUser) {
        this.ordenRepository = ordenRepository;
        this.detalleRepository = detalleRepository;
        this.claveRepository = claveRepository;
        this.cipher = cipher;
        this.currentUser = currentUser;
    }

    public List<OrdenResponse> historialDelCliente() {
        return ordenRepository.findByUsuario(currentUser.id()).stream()
                .map(this::toResponse).toList();
    }

    public List<OrdenResponse> todasLasOrdenes() {
        return ordenRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "fechaCreacion")).stream()
                .map(this::toResponse).toList();
    }

    public OrdenResponse porId(Long id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Orden no encontrada."));
        if (!esAdmin() && !orden.getUsuario().getId().equals(currentUser.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No puedes consultar la orden de otro cliente.");
        }
        return toResponse(orden);
    }

    private boolean esAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(autoridad -> "ROLE_ADMIN".equals(autoridad.getAuthority()));
    }

    private OrdenResponse toResponse(Orden orden) {
        return OrdenMapper.toResponse(orden, detalleRepository.findByOrdenId(orden.getId()),
                claveRepository, cipher, true);
    }
}
