package com.tiendajuegos.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 160)
    private String correo;

    @Column(name = "contrasena_hash", nullable = false)
    private String contrasenaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role rol;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private Instant fechaRegistro = Instant.now();

    @Column(name = "eliminado", nullable = false)
    private boolean eliminado = false;

    @Column(name = "fecha_eliminacion")
    private Instant fechaEliminacion;

    protected Usuario() {
    }

    public Usuario(String nombre, String correo, String contrasenaHash, Role rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
    }

    /**
     * Anonimiza la cuenta sin borrar el historial: las órdenes y reseñas siguen
     * apuntando al mismo registro, pero ya no se puede iniciar sesión con él.
     */
    public void anonimizar(String nuevoHash) {
        this.nombre = "Usuario eliminado";
        this.correo = "eliminado-" + id + "@invalid.local";
        this.contrasenaHash = nuevoHash;
        this.eliminado = true;
        this.fechaEliminacion = Instant.now();
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getContrasenaHash() { return contrasenaHash; }
    public Role getRol() { return rol; }
    public boolean isEliminado() { return eliminado; }
    public Instant getFechaEliminacion() { return fechaEliminacion; }
}
