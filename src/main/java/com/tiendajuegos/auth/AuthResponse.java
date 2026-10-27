package com.tiendajuegos.auth;

public record AuthResponse(Long id, String nombre, String correo, String rol, String token) {
}
