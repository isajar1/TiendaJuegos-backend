package com.tiendajuegos.auth;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Correo o contrasena incorrectos");
    }
}
