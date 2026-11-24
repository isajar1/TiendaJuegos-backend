package com.tiendajuegos.catalog;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.dto.ApiDtos.CatalogoItem;
import com.tiendajuegos.dto.ApiDtos.JuegoRequest;
import com.tiendajuegos.dto.ApiDtos.JuegoResponse;
import com.tiendajuegos.inventory.ClaveDigitalRepository;
import com.tiendajuegos.inventory.EstadoClave;
import com.tiendajuegos.social.ResenaRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final JuegoRepository juegoRepository;
    private final PlataformaRepository plataformaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ClaveDigitalRepository claveDigitalRepository;
    private final ResenaRepository resenaRepository;
    private final AuditService auditService;

    public CatalogService(JuegoRepository juegoRepository,
                          PlataformaRepository plataformaRepository,
                          CategoriaRepository categoriaRepository,
                          ClaveDigitalRepository claveDigitalRepository,
                          ResenaRepository resenaRepository,
                          AuditService auditService) {
        this.juegoRepository = juegoRepository;
        this.plataformaRepository = plataformaRepository;
        this.categoriaRepository = categoriaRepository;
        this.claveDigitalRepository = claveDigitalRepository;
        this.resenaRepository = resenaRepository;
        this.auditService = auditService;
    }

    public List<JuegoResponse> listar(String titulo, Long plataformaId, Long categoriaId, String region,
                                      BigDecimal minPrecio, BigDecimal maxPrecio, EstadoJuego estado) {
        Specification<Juego> filtros = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (titulo != null && !titulo.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("titulo")), "%" + titulo.toLowerCase() + "%"));
            }
            if (plataformaId != null) {
                predicates.add(cb.equal(root.get("plataforma").get("id"), plataformaId));
            }
            if (categoriaId != null) {
                predicates.add(cb.equal(root.get("categoria").get("id"), categoriaId));
            }
            if (region != null && !region.isBlank()) {
                predicates.add(cb.equal(root.get("region"), region));
            }
            if (minPrecio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("precio"), minPrecio));
            }
            if (maxPrecio != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("precio"), maxPrecio));
            }
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return juegoRepository.findAll(filtros, Sort.by("id")).stream().map(this::toResponse).toList();
    }

    public JuegoResponse detalle(Long id) {
        return toResponse(buscarJuego(id));
    }

    @Transactional
    public JuegoResponse crear(JuegoRequest request) {
        validarJuego(request);
        Plataforma plataforma = plataformaRepository.findById(request.plataformaId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "La plataforma no existe."));
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "La categoria no existe."));
        Juego juego = new Juego(request.titulo().trim(), request.descripcion(), request.precio(),
                request.region() == null || request.region().isBlank() ? "Global" : request.region().trim(),
                plataforma, categoria);
        Juego guardado = juegoRepository.save(juego);
        auditService.registrar("CREAR_JUEGO", "juegos");
        return toResponse(guardado);
    }

    @Transactional
    public JuegoResponse actualizar(Long id, JuegoRequest request) {
        validarJuego(request);
        Juego juego = buscarJuego(id);
        juego.setTitulo(request.titulo().trim());
        juego.setDescripcion(request.descripcion());
        juego.setPrecio(request.precio());
        juego.setRegion(request.region() == null || request.region().isBlank() ? "Global" : request.region().trim());
        juego.setPlataforma(plataformaRepository.findById(request.plataformaId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "La plataforma no existe.")));
        juego.setCategoria(categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "La categoria no existe.")));
        Juego guardado = juegoRepository.save(juego);
        auditService.registrar("EDITAR_JUEGO", "juegos");
        return toResponse(guardado);
    }

    @Transactional
    public void deshabilitar(Long id) {
        Juego juego = buscarJuego(id);
        juego.setEstado(EstadoJuego.inactivo);
        juegoRepository.save(juego);
        auditService.registrar("DESHABILITAR_JUEGO", "juegos");
    }

    public List<CatalogoItem> categorias() {
        return categoriaRepository.findAll(Sort.by("nombre")).stream()
                .map(c -> new CatalogoItem(c.getId(), c.getNombre())).toList();
    }

    public List<CatalogoItem> plataformas() {
        return plataformaRepository.findAll(Sort.by("nombre")).stream()
                .map(p -> new CatalogoItem(p.getId(), p.getNombre())).toList();
    }

    @Transactional
    public CatalogoItem crearCategoria(String nombre) {
        String valor = nombreRequerido(nombre);
        if (categoriaRepository.existsByNombreIgnoreCase(valor)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una categoria con ese nombre.");
        }
        Categoria guardada = categoriaRepository.save(new Categoria(valor));
        auditService.registrar("CREAR_CATEGORIA", "categorias");
        return new CatalogoItem(guardada.getId(), guardada.getNombre());
    }

    @Transactional
    public CatalogoItem crearPlataforma(String nombre) {
        String valor = nombreRequerido(nombre);
        if (plataformaRepository.existsByNombreIgnoreCase(valor)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una plataforma con ese nombre.");
        }
        Plataforma guardada = plataformaRepository.save(new Plataforma(valor));
        auditService.registrar("CREAR_PLATAFORMA", "plataformas");
        return new CatalogoItem(guardada.getId(), guardada.getNombre());
    }

    private Juego buscarJuego(Long id) {
        return juegoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Juego no encontrado."));
    }

    private void validarJuego(JuegoRequest request) {
        if (request == null || request.titulo() == null || request.titulo().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El titulo del juego es obligatorio.");
        }
        if (request.precio() == null || request.precio().compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor o igual a cero.");
        }
        if (request.plataformaId() == null || request.categoriaId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Plataforma y categoria son obligatorias.");
        }
    }

    private String nombreRequerido(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio.");
        }
        return nombre.trim();
    }

    private JuegoResponse toResponse(Juego juego) {
        var resenas = resenaRepository.findByJuegoId(juego.getId());
        double promedio = resenas.isEmpty() ? 0.0
                : resenas.stream().mapToInt(r -> r.getCalificacion()).average().orElse(0.0);
        boolean disponible = claveDigitalRepository.countByJuegoIdAndEstado(juego.getId(), EstadoClave.disponible) > 0;
        return new JuegoResponse(
                juego.getId(),
                juego.getTitulo(),
                juego.getDescripcion(),
                juego.getPrecio(),
                juego.getRegion(),
                juego.getPlataforma().getNombre(),
                juego.getCategoria().getNombre(),
                juego.getEstado().name(),
                Math.round(promedio * 100.0) / 100.0,
                resenas.size(),
                disponible
        );
    }
}
