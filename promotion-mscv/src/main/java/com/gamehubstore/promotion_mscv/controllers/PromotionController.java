package com.gamehubstore.promotion_mscv.controllers;
import com.gamehubstore.promotion_mscv.models.*; import com.gamehubstore.promotion_mscv.services.PromotionService; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.math.BigDecimal; import java.util.*;
@RestController @RequestMapping("/api/v1/promotions") public class PromotionController { private final PromotionService service; public PromotionController(PromotionService s){service=s;}
 @GetMapping public List<Promotion> all(){return service.all();} @GetMapping("/active") public List<Promotion> active(){return service.active();}
 @GetMapping("/{id}") public Promotion byId(@PathVariable Long id){return service.byId(id);} @GetMapping("/code/{code}") public Promotion byCode(@PathVariable String code){return service.byCode(code);}
 @PostMapping public ResponseEntity<Promotion> create(@Valid @RequestBody Promotion p){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p));}
 @PutMapping("/{id}") public Promotion update(@PathVariable Long id,@Valid @RequestBody Promotion p){return service.update(id,p);}
 @DeleteMapping("/{id}") public ResponseEntity<Void> deactivate(@PathVariable Long id){service.deactivate(id);return ResponseEntity.noContent().build();}
 @PostMapping("/validate") public Map<String,BigDecimal> validate(@RequestParam @NotBlank String code,@RequestParam @DecimalMin("0.00") BigDecimal total){return Map.of("discount",service.validateAndCalculate(code,total));}
 @PutMapping("/reservations/{orderId}") public PromotionReservation reserve(@PathVariable Long orderId,@RequestParam String code,@RequestParam BigDecimal total){return service.reserve(orderId,code,total);}
 @PutMapping("/reservations/{orderId}/confirm") public PromotionReservation commit(@PathVariable Long orderId){return service.commit(orderId);}
 @DeleteMapping("/reservations/{orderId}") public ResponseEntity<Void> release(@PathVariable Long orderId){service.release(orderId);return ResponseEntity.noContent().build();}
}
