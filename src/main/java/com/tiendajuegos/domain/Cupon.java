package com.tiendajuegos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cupones")
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "porcentaje_descuento", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private boolean activo = true;

    protected Cupon() {
    }

    public Cupon(String codigo, BigDecimal porcentajeDescuento, LocalDateTime fechaExpiracion, boolean activo) {
        this.codigo = codigo;
        this.porcentajeDescuento = porcentajeDescuento;
        this.fechaExpiracion = fechaExpiracion;
        this.activo = activo;
    }

    public boolean isVigente() {
        return activo && fechaExpiracion != null && !fechaExpiracion.isBefore(LocalDateTime.now());
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public BigDecimal getPorcentajeDescuento() { return porcentajeDescuento; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public boolean isActivo() { return activo; }

    public void setActivo(boolean activo) { this.activo = activo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
}
