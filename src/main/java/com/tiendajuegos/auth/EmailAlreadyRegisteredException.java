package com.tiendajuegos.auth;

public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException() {
        super("El correo ya esta registrado");
    }
}
