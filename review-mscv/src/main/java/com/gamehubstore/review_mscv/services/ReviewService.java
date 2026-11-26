package com.gamehubstore.review_mscv.services; import com.gamehubstore.review_mscv.models.ResumenResenasDTO; import com.gamehubstore.review_mscv.models.Review; import com.gamehubstore.review_mscv.repositories.ReviewRepository; import com.gamehubstore.review_mscv.client.OrderClient; import org.slf4j.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;
@Service public class ReviewService { private static final Logger log=LoggerFactory.getLogger(ReviewService.class); private final ReviewRepository repo; private final OrderClient orders;
 public ReviewService(ReviewRepository r,OrderClient o){repo=r;orders=o;} public List<Review> all(){return repo.findAll();} public List<Review> byProduct(Long id){return repo.findByProductIdAndActiveTrue(id);} public List<Review> byUser(Long id){return repo.findByUserId(id);} public Review byId(Long id){return repo.findById(id).orElseThrow(()->new NoSuchElementException("Reseña no encontrada: "+id));}
 @Transactional public Review create(Review r){Map<String,Object> order=orders.findOrder(r.getOrderId()); Object owner=order.get("userId"); if(owner==null||!String.valueOf(owner).equals(String.valueOf(r.getUserId())))throw new IllegalArgumentException("La orden no pertenece al usuario"); if(!"PAGADA".equalsIgnoreCase(String.valueOf(order.get("estado"))))throw new IllegalArgumentException("Solo se aceptan reseñas de órdenes pagadas"); Object details=order.get("details"); boolean purchased=details instanceof List<?> list&&list.stream().anyMatch(x->x instanceof Map<?,?> m&&String.valueOf(m.get("idProducto")).equals(String.valueOf(r.getProductId()))); if(!purchased)throw new IllegalArgumentException("El producto no pertenece a la orden indicada"); if(repo.existsByUserIdAndOrderIdAndProductId(r.getUserId(),r.getOrderId(),r.getProductId()))throw new IllegalArgumentException("Ya existe reseña para la compra y producto"); r.setActive(true); log.info("Creando reseña usuario={} producto={}",r.getUserId(),r.getProductId()); return repo.save(r);}
 @Transactional public Review update(Long id,Review data){Review old=byId(id); old.setRating(data.getRating()); old.setComment(data.getComment()); return repo.save(old);} @Transactional public void moderate(Long id){Review r=byId(id);r.setActive(false);r.setEstado("OCULTA");repo.save(r);}
 @Transactional(readOnly=true) public List<ResumenResenasDTO> resumen(List<Long> productoIds){
  if(productoIds==null||productoIds.isEmpty()) return List.of();
  return repo.resumenPorProductos(productoIds).stream()
   .map(row->new ResumenResenasDTO(((Number)row[0]).longValue(),row[1]!=null?((Number)row[1]).doubleValue():0.0,((Number)row[2]).longValue()))
   .toList();
 }
 @Transactional(readOnly=true) public List<Review> byEstado(String estado){return repo.findByEstadoIgnoreCase(estado);}
 @Transactional public Review moderar(Long id,String estado,String motivo){
  Review r=byId(id);
  String norm=(estado==null||estado.isBlank())?"PUBLICADA":estado.trim().toUpperCase(Locale.ROOT);
  r.setEstado(norm);
  r.setActive("PUBLICADA".equalsIgnoreCase(norm));
  r.setMotivoModeracion(motivo);
  return repo.save(r);
 }
}
