package com.gamehubstore.inventory_mscv.exceptions;

import com.gamehubstore.inventory_mscv.models.dtos.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionController {

    // 1. Captura cuando un ID de inventario o producto no existe (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorDetails error = ErrorDetails.builder()
                .codigo("NOT_FOUND")
                .mensaje(ex.getMessage())
                .detalles("El recurso solicitado no existe en los registros de inventario.")
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 2. Captura reglas de negocio de inventario (422)
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorDetails> handleBusinessRule(BusinessRuleException ex) {
        ErrorDetails error = ErrorDetails.builder()
                .codigo("UNPROCESSABLE_ENTITY")
                .mensaje(ex.getMessage())
                .detalles("Operación de inventario rechazada por reglas del negocio.")
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // 3. 🎯 AQUÍ SE SOLUCIONA TU ERROR ACTUAL (Captura el error de validación del @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Extrae el mensaje: "stockMinimo: El stock mínimo es obligatorio para alertas de reposición"
        String camposConError = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorDetails error = ErrorDetails.builder()
                .codigo("BAD_REQUEST")
                .mensaje("Error de validación en los datos de entrada del inventario.")
                .detalles(camposConError) // Muestra exactamente qué campo falló
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}