package com.gamehubstore.notification_mscv.controllers;
import org.slf4j.*; import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestControllerAdvice public class ApiExceptionHandler { private static final Logger log=LoggerFactory.getLogger(ApiExceptionHandler.class);
 @ExceptionHandler(NoSuchElementException.class) public ResponseEntity<Map<String,String>> missing(NoSuchElementException e){return ResponseEntity.status(404).body(Map.of("error",e.getMessage()));}
 @ExceptionHandler({IllegalArgumentException.class,MethodArgumentNotValidException.class}) public ResponseEntity<Map<String,String>> bad(Exception e){log.warn("Solicitud inválida: {}",e.getMessage()); return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()==null?"Solicitud inválida":e.getMessage()));}
}
