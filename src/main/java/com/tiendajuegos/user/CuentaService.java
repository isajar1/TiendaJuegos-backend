package com.tiendajuegos.user;

import com.tiendajuegos.audit.AuditService;
import com.tiendajuegos.common.ApiException;
import com.tiendajuegos.repository.CarritoItemRepository;
import com.tiendajuegos.repository.CarritoRepository;
import com.tiendajuegos.repository.ListaDeseosRepository;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestión de la cuenta del usuario autenticado. */
@Service
public class CuentaService {

    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository itemRepository;
    private final ListaDeseosRepository listaDeseosRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public CuentaService(UsuarioRepository usuarioRepository, CarritoRepository carritoRepository,
                         CarritoItemRepository itemRepository, ListaDeseosRepository listaDeseosRepository,
                         PasswordEncoder passwordEncoder, AuditService auditService) {
        this.usuarioRepository = usuarioRepository;
        this.carritoRepository = carritoRepository;
        this.itemRepository = itemRepository;
        this.listaDeseosRepository = listaDeseosRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    /**
     * Elimina la cuenta de forma irreversible pero conserva el historial:
     * anonimiza el registro, limpia carrito y deseos, y deja órdenes y reseñas
     * apuntando al mismo usuario con el nombre «Usuario eliminado».
     */
    @Transactional
    public void eliminarMiCuenta(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));
        if (usuario.isEliminado()) {
            return;
        }

        carritoRepository.findByUsuarioId(usuarioId).ifPresent(carrito -> {
            itemRepository.deleteAll(itemRepository.findByCarritoIdOrderByIdAsc(carrito.getId()));
            carritoRepository.delete(carrito);
        });
        listaDeseosRepository.deleteAll(listaDeseosRepository.findByUsuario(usuarioId));

        usuario.anonimizar(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuarioRepository.save(usuario);
        auditService.registrar(usuarioId, "cuenta eliminada", "usuario");
    }
}
