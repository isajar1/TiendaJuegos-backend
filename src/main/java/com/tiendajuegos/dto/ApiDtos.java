package com.tiendajuegos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** DTOs compartidos por los controladores de la API. */
public final class ApiDtos {

    private ApiDtos() {
    }

    public record Mensaje(String message) {
    }

    public record NombreRequest(String nombre) {
    }

    public record CatalogoItem(Long id, String nombre) {
    }

    public record JuegoRequest(
            String titulo,
            String descripcion,
            BigDecimal precio,
            String region,
            Long plataformaId,
            Long categoriaId
    ) {
    }

    public record JuegoResponse(
            Long id,
            String titulo,
            String descripcion,
            BigDecimal precio,
            String region,
            String plataforma,
            String categoria,
            String estado,
            double calificacionPromedio,
            int totalResenas,
            boolean disponible
    ) {
    }

    public record ClavesRequest(List<String> claves) {
    }

    public record CuponRequest(
            String codigo,
            BigDecimal porcentajeDescuento,
            LocalDateTime fechaExpiracion,
            Boolean activo
    ) {
    }

    public record CuponResponse(
            Long id,
            String codigo,
            BigDecimal porcentajeDescuento,
            LocalDateTime fechaExpiracion,
            boolean activo
    ) {
    }

    public record ItemCarritoRequest(Long juegoId) {
    }

    public record CarritoRequest(List<ItemCarritoRequest> items, String codigoCupon) {
    }

    public record CarritoItemResponse(Long juegoId, String titulo, BigDecimal precioUnitario, boolean disponible) {
    }

    public record CarritoResponse(
            List<CarritoItemResponse> items,
            BigDecimal subtotal,
            BigDecimal descuento,
            BigDecimal total,
            String cupon
    ) {
    }

    public record CheckoutRequest(List<ItemCarritoRequest> items, String codigoCupon, Boolean aprobarPago) {
    }

    public record DetalleOrdenResponse(Long juegoId, String titulo, BigDecimal precioUnitario, String clave) {
    }

    public record OrdenResponse(
            Long id,
            LocalDateTime fechaCreacion,
            BigDecimal total,
            String estadoPago,
            String cupon,
            List<DetalleOrdenResponse> detalles
    ) {
    }

    public record ResenaRequest(int calificacion, String comentario) {
    }

    public record ResenaResponse(
            Long id,
            Long juegoId,
            String usuario,
            int calificacion,
            String comentario,
            LocalDateTime fecha
    ) {
    }

    public record ReporteItemResponse(String etiqueta, long cantidad, BigDecimal total) {
    }

    public record PerfilRequest(String nombre, String contrasenaActual, String contrasenaNueva) {
    }

    public record PerfilResponse(Long id, String nombre, String correo, String rol) {
    }

    public record AuditoriaResponse(Long id, String usuario, String accion, String entidad, LocalDateTime fechaHora) {
    }
}
