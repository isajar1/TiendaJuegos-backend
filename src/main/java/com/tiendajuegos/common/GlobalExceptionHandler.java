package com.tiendajuegos.common;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Convierte cualquier error de la API en {"message":"..."}. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> apiException(ApiException exception) {
        return response(exception.getStatus(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Datos inválidos");
        return response(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, ConstraintViolationException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> badRequest(Exception exception) {
        return response(HttpStatus.BAD_REQUEST, "Solicitud inválida");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataConflict(DataIntegrityViolationException exception) {
        return response(HttpStatus.CONFLICT, "El registro ya existe o viola una restricción de datos");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDenied(AccessDeniedException exception) {
        return response(HttpStatus.FORBIDDEN, "Acceso denegado");
    }

    @ExceptionHandler({AuthenticationException.class, AuthenticationCredentialsNotFoundException.class})
    public ResponseEntity<ErrorResponse> unauthenticated(Exception exception) {
        return response(HttpStatus.UNAUTHORIZED, "Autenticación requerida");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception exception) {
        log.error("Error no controlado en la API", exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(message));
    }

    /** Utilidad para que los tests verifiquen la forma exacta del cuerpo. */
    public static Map<String, String> body(String message) {
        return Map.of("message", message);
    }
}
