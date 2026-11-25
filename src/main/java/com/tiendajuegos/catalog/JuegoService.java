package com.tiendajuegos.catalog;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.catalog.CatalogDtos.ClavesDisponiblesResponse;
import com.tiendajuegos.catalog.CatalogDtos.ClavesRequest;
import com.tiendajuegos.catalog.CatalogDtos.ClavesResponse;
import com.tiendajuegos.catalog.CatalogDtos.JuegoRequest;
import com.tiendajuegos.catalog.CatalogDtos.JuegoResponse;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.crypto.CryptoService;
import com.tiendajuegos.domain.Categoria;
import com.tiendajuegos.domain.ClaveDigital;
import com.tiendajuegos.domain.EstadoClave;
import com.tiendajuegos.domain.EstadoJuego;
import com.tiendajuegos.domain.Juego;
import com.tiendajuegos.domain.Plataforma;
import com.tiendajuegos.domain.Resena;
import com.tiendajuegos.repository.CategoriaRepository;
import com.tiendajuegos.repository.ClaveDigitalRepository;
import com.tiendajuegos.repository.JuegoRepository;
import com.tiendajuegos.repository.PlataformaRepository;
import com.tiendajuegos.repository.ResenaRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JuegoService {

    private final JuegoRepository juegoRepository;
    private final PlataformaRepository plataformaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ClaveDigitalRepository claveRepository;
    private final ResenaRepository resenaRepository;
    private final CryptoService cryptoService;
    private final AuditService auditService;

    public JuegoService(JuegoRepository juegoRepository, PlataformaRepository plataformaRepository,
                        CategoriaRepository categoriaRepository, ClaveDigitalRepository claveRepository,
                        ResenaRepository resenaRepository, CryptoService cryptoService,
                        AuditService auditService) {
        this.juegoRepository = juegoRepository;
        this.plataformaRepository = plataformaRepository;
        this.categoriaRepository = categoriaRepository;
        this.claveRepository = claveRepository;
        this.resenaRepository = resenaRepository;
        this.cryptoService = cryptoService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<JuegoResponse> listar(String q, String categoria, String plataforma, String region) {
        return juegoRepository.findAllByOrderByIdAsc().stream()
                .filter(Juego::isActivo)
                .filter(juego -> matchesText(q, juego))
                .filter(juego -> matchesName(categoria, juego.getCategoria().getNombre()))
                .filter(juego -> matchesName(plataforma, juego.getPlataforma().getNombre()))
                .filter(juego -> matchesName(region, juego.getRegion()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public JuegoResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public JuegoResponse crear(JuegoRequest request, Long usuarioId) {
        Plataforma plataforma = plataformaRepository.findById(request.plataformaId())
                .orElseThrow(() -> ApiException.notFound("Plataforma no encontrada"));
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> ApiException.notFound("Categoría no encontrada"));
        Juego juego = new Juego(request.titulo().trim(), request.descripcion(), request.precio(),
                normalizarRegion(request.region()), plataforma, categoria);
        Juego guardado = juegoRepository.save(juego);
        auditService.registrar(usuarioId, "juego creado", "juego");
        return toResponse(guardado);
    }

    @Transactional
    public JuegoResponse actualizar(Long id, JuegoRequest request, Long usuarioId) {
        Juego juego = buscar(id);
        Plataforma plataforma = plataformaRepository.findById(request.plataformaId())
                .orElseThrow(() -> ApiException.notFound("Plataforma no encontrada"));
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> ApiException.notFound("Categoría no encontrada"));
        juego.setTitulo(request.titulo().trim());
        juego.setDescripcion(request.descripcion());
        juego.setPrecio(request.precio());
        juego.setRegion(normalizarRegion(request.region()));
        juego.setPlataforma(plataforma);
        juego.setCategoria(categoria);
        Juego guardado = juegoRepository.save(juego);
        auditService.registrar(usuarioId, "juego editado", "juego");
        return toResponse(guardado);
    }

    @Transactional
    public void desactivar(Long id, Long usuarioId) {
        Juego juego = buscar(id);
        juego.setEstado(EstadoJuego.inactivo);
        juegoRepository.save(juego);
        auditService.registrar(usuarioId, "juego desactivado", "juego");
    }

    @Transactional
    public ClavesResponse agregarClaves(Long juegoId, ClavesRequest request, Long usuarioId) {
        Juego juego = buscar(juegoId);
        for (String clave : request.claves()) {
            claveRepository.save(new ClaveDigital(juego, cryptoService.encrypt(clave.trim())));
        }
        auditService.registrar(usuarioId, "claves cargadas", "clave_digital");
        return new ClavesResponse(request.claves().size());
    }

    @Transactional(readOnly = true)
    public ClavesDisponiblesResponse contarClaves(Long juegoId) {
        buscar(juegoId);
        return new ClavesDisponiblesResponse(
                claveRepository.countByJuegoIdAndEstado(juegoId, EstadoClave.disponible),
                claveRepository.countByJuegoIdAndEstado(juegoId, EstadoClave.vendida));
    }

    private Juego buscar(Long id) {
        return juegoRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Juego no encontrado"));
    }

    private JuegoResponse toResponse(Juego juego) {
        long disponibles = juego.getId() == null ? 0
                : claveRepository.countByJuegoIdAndEstado(juego.getId(), EstadoClave.disponible);
        List<Resena> resenas = juego.getId() == null ? List.of() : resenaRepository.findByJuegoIdOrderByFechaDesc(juego.getId());
        double promedio = resenas.stream()
                .mapToInt(Resena::getCalificacion)
                .average()
                .orElse(0.0);
        return new JuegoResponse(
                juego.getId(),
                juego.getTitulo(),
                juego.getDescripcion(),
                juego.getPrecio(),
                juego.getRegion(),
                juego.getPlataforma().getNombre(),
                juego.getCategoria().getNombre(),
                juego.isActivo(),
                disponibles,
                promedio,
                resenas.size());
    }

    private boolean matchesText(String q, Juego juego) {
        if (q == null || q.isBlank()) return true;
        String texto = q.toLowerCase(Locale.ROOT);
        return juego.getTitulo().toLowerCase(Locale.ROOT).contains(texto)
                || (juego.getDescripcion() != null
                && juego.getDescripcion().toLowerCase(Locale.ROOT).contains(texto));
    }

    private boolean matchesName(String filtro, String nombre) {
        return filtro == null || filtro.isBlank() || nombre.equalsIgnoreCase(filtro);
    }

    private String normalizarRegion(String region) {
        return region == null || region.isBlank() ? "Global" : region.trim();
    }
}
