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
import com.tiendajuegos.repository.ClaveDigitalRepository.ConteoPorJuego;
import com.tiendajuegos.repository.JuegoRepository;
import com.tiendajuegos.repository.PlataformaRepository;
import com.tiendajuegos.repository.ResenaRepository;
import com.tiendajuegos.repository.ResenaRepository.ResumenPorJuego;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        String categoriaNombre = resolverCategoria(categoria);
        String plataformaNombre = resolverPlataforma(plataforma);

        // Tres consultas agrupadas para todo el catálogo, en lugar de cuatro por juego.
        Map<Long, Long> clavesDisponibles = new HashMap<>();
        for (ConteoPorJuego conteo : claveRepository.contarPorEstado(EstadoClave.disponible)) {
            clavesDisponibles.put(conteo.getJuegoId(), conteo.getTotal());
        }
        Map<Long, double[]> resenas = new HashMap<>();
        for (ResumenPorJuego resumen : resenaRepository.resumenPorJuego()) {
            resenas.put(resumen.getJuegoId(), new double[]{resumen.getTotal(), resumen.getPromedio()});
        }

        return juegoRepository.findAllConRelaciones().stream()
                .filter(Juego::isActivo)
                .filter(juego -> matchesText(q, juego))
                .filter(juego -> matchesName(categoriaNombre, juego.getCategoria().getNombre()))
                .filter(juego -> matchesName(plataformaNombre, juego.getPlataforma().getNombre()))
                .filter(juego -> matchesName(region, juego.getRegion()))
                .map(juego -> toResponse(juego, clavesDisponibles, resenas))
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
        Map<Long, Long> claves = new HashMap<>();
        Map<Long, double[]> resenas = new HashMap<>();
        if (juego.getId() != null) {
            claves.put(juego.getId(),
                    claveRepository.countByJuegoIdAndEstado(juego.getId(), EstadoClave.disponible));
            List<Resena> lista = resenaRepository.findByJuegoIdOrderByFechaDesc(juego.getId());
            double promedio = lista.stream().mapToInt(Resena::getCalificacion).average().orElse(0.0);
            resenas.put(juego.getId(), new double[]{lista.size(), promedio});
        }
        return toResponse(juego, claves, resenas);
    }

    private JuegoResponse toResponse(Juego juego, Map<Long, Long> clavesDisponibles,
                                     Map<Long, double[]> resenas) {
        long id = juego.getId() == null ? -1L : juego.getId();
        double[] datosResenas = resenas.getOrDefault(id, new double[]{0.0, 0.0});
        return new JuegoResponse(
                juego.getId(),
                juego.getTitulo(),
                juego.getDescripcion(),
                juego.getPrecio(),
                juego.getRegion(),
                juego.getPlataforma().getNombre(),
                juego.getCategoria().getNombre(),
                juego.isActivo(),
                clavesDisponibles.getOrDefault(id, 0L),
                datosResenas[1],
                (long) datosResenas[0]);
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

    /** Acepta el nombre o el ID de la categoría, para que ningún cliente quede fuera del filtro. */
    private String resolverCategoria(String filtro) {
        if (filtro == null || filtro.isBlank()) return filtro;
        String valor = filtro.trim();
        try {
            return categoriaRepository.findById(Long.parseLong(valor))
                    .map(Categoria::getNombre)
                    .orElse(valor);
        } catch (NumberFormatException excepcion) {
            return valor;
        }
    }

    /** Acepta el nombre o el ID de la plataforma. */
    private String resolverPlataforma(String filtro) {
        if (filtro == null || filtro.isBlank()) return filtro;
        String valor = filtro.trim();
        try {
            return plataformaRepository.findById(Long.parseLong(valor))
                    .map(Plataforma::getNombre)
                    .orElse(valor);
        } catch (NumberFormatException excepcion) {
            return valor;
        }
    }

    private String normalizarRegion(String region) {
        return region == null || region.isBlank() ? "Global" : region.trim();
    }
}
