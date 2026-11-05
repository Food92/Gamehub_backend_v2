package com.gamehubstore.shipping_mscv.exceptions; // 👈 Cambiado a paquete de excepciones

import com.gamehubstore.shipping_mscv.exceptions.ResourceNotFoundException;
import com.gamehubstore.shipping_mscv.exceptions.ShippingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // 👈 La anotación debe ir sobre la clase principal del archivo
public class GlobalExceptionHandler {

    // 1. Captura las reglas de negocio rotas de Logística (Error 422)
    @ExceptionHandler(ShippingException.class)
    public ResponseEntity<Map<String, Object>> handleShippingException(ShippingException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        response.put("error", "Error de Regla de Negocio Logística");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // 2. Captura los IDs no localizados (Error 404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(ResourceNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Recurso No Encontrado");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 3. Captura las validaciones de Beans de tus DTOs (@NotBlank, @NotNull) (Error 400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validación Fallida");
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}