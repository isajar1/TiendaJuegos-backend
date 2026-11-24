package com.tiendajuegos.common;

import com.tiendajuegos.dto.ApiDtos;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiDtos.Mensaje> apiError(ApiException exception) {
        return ResponseEntity.status(exception.status()).body(new ApiDtos.Mensaje(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiDtos.Mensaje> validation(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(new ApiDtos.Mensaje("Datos invalidos: revisa los campos obligatorios."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiDtos.Mensaje> unreadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(new ApiDtos.Mensaje("El cuerpo de la peticion no es valido."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiDtos.Mensaje> duplicated(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiDtos.Mensaje("El registro ya existe o viola una restriccion de la base de datos."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiDtos.Mensaje> denied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiDtos.Mensaje("No tienes permisos para realizar esta operacion."));
    }
}
