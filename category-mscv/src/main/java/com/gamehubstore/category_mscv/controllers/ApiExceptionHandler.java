package com.gamehubstore.category_mscv.controllers;

import com.gamehubstore.category_mscv.exceptions.CategoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        log.warn("Validación de categoría fallida: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }
    @ExceptionHandler(CategoryException.class)
    public ResponseEntity<Map<String,String>> handleBusiness(CategoryException ex) {
        log.warn("Regla de categoría: {}", ex.getMessage());
        return ResponseEntity.unprocessableEntity().body(Map.of("error", ex.getMessage()));
    }
}
