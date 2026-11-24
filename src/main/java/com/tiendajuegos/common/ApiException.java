package com.tiendajuegos.common;

import org.springframework.http.HttpStatus;

/** Error de dominio con el estado HTTP que debe devolverse al cliente. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
