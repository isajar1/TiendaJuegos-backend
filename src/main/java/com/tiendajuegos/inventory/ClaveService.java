package com.tiendajuegos.inventory;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.catalog.Juego;
import com.tiendajuegos.catalog.JuegoRepository;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.dto.ApiDtos.ClavesRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClaveService {

    private final ClaveDigitalRepository claveRepository;
    private final JuegoRepository juegoRepository;
    private final KeyCipherService cipher;
    private final AuditService auditService;

    public ClaveService(ClaveDigitalRepository claveRepository,
                        JuegoRepository juegoRepository,
                        KeyCipherService cipher,
                        AuditService auditService) {
        this.claveRepository = claveRepository;
        this.juegoRepository = juegoRepository;
        this.cipher = cipher;
        this.auditService = auditService;
    }

    @Transactional
    public int cargar(Long juegoId, ClavesRequest request) {
        Juego juego = juegoRepository.findById(juegoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Juego no encontrado."));
        if (request == null || request.claves() == null || request.claves().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debes enviar al menos una clave.");
        }

        Set<String> nuevas = new HashSet<>();
        for (String clave : request.claves()) {
            if (clave == null || clave.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Las claves no pueden estar vacias.");
            }
            if (!nuevas.add(clave.trim())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Hay claves repetidas en la lista enviada.");
            }
        }

        List<String> existentes = claveRepository.findByJuegoId(juegoId).stream()
                .map(clave -> cipher.descifrar(clave.getClaveCifrada())).toList();
        if (nuevas.stream().anyMatch(existentes::contains)) {
            throw new ApiException(HttpStatus.CONFLICT, "Una de las claves ya existe en el sistema.");
        }

        claveRepository.saveAll(nuevas.stream()
                .map(clave -> new ClaveDigital(juego, cipher.cifrar(clave))).toList());
        auditService.registrar("CARGA_CLAVES", "claves_digitales");
        return nuevas.size();
    }

    @Transactional(readOnly = true)
    public String descifrar(Long claveId) {
        ClaveDigital clave = claveRepository.findById(claveId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Clave no encontrada."));
        return cipher.descifrar(clave.getClaveCifrada());
    }
}
