package com.tiendajuegos.domain;

import com.tiendajuegos.user.Usuario;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "carrito")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cupon_id")
    private Cupon cupon;

    protected Carrito() {
    }

    public Carrito(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Cupon getCupon() { return cupon; }

    public void setCupon(Cupon cupon) { this.cupon = cupon; }
}
