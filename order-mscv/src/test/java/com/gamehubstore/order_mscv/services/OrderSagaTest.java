package com.gamehubstore.order_mscv.services;
import com.gamehubstore.order_mscv.client.*; import com.gamehubstore.order_mscv.exceptions.OrderException; import com.gamehubstore.order_mscv.models.Order; import com.gamehubstore.order_mscv.models.dtos.*; import com.gamehubstore.order_mscv.repositories.OrderRepository;
import org.junit.jupiter.api.Test; import java.util.*; import static org.junit.jupiter.api.Assertions.*; import static org.mockito.ArgumentMatchers.*; import static org.mockito.Mockito.*;
class OrderSagaTest {
 @Test void compensatesEveryLineWhenLaterInventoryReservationFails(){
  OrderRepository orders=mock(OrderRepository.class); InventoryClient inventory=mock(InventoryClient.class); UserClient users=mock(UserClient.class); ProductClient products=mock(ProductClient.class); PromotionClient promotions=mock(PromotionClient.class);
  UserClientDTO user=new UserClientDTO();user.setEstado(true);when(users.getUserById(7L)).thenReturn(user);
  ProductDTO product=new ProductDTO();product.setEstado(true);product.setPrecio(10.0);when(products.findById(anyLong())).thenReturn(product);
  when(orders.saveAndFlush(any(Order.class))).thenAnswer(inv->{Order o=inv.getArgument(0);if(o.getOrderId()==null)o.setOrderId(91L);return o;});
  when(inventory.reservar(91L,2L,1L)).thenReturn(new Object());when(inventory.reservar(91L,3L,1L)).thenThrow(new RuntimeException("inventory unavailable"));
  OrderServiceImpl service=new OrderServiceImpl(orders,inventory,users,products,promotions);
  OrderDTO request=new OrderDTO();request.setIdUsuario(7L);request.setDetails(List.of(line(2L),line(3L)));
  assertThrows(OrderException.class,()->service.save(request));
  verify(inventory).liberar(91L,2L);verify(inventory).liberar(91L,3L);verify(promotions).release(91L);
  verify(orders,times(3)).saveAndFlush(any(Order.class));
 }

 @Test void retriesCompensationWhenAReleaseCallFails(){
  OrderRepository orders=mock(OrderRepository.class); InventoryClient inventory=mock(InventoryClient.class); UserClient users=mock(UserClient.class); ProductClient products=mock(ProductClient.class); PromotionClient promotions=mock(PromotionClient.class);
  UserClientDTO user=new UserClientDTO();user.setEstado(true);when(users.getUserById(7L)).thenReturn(user);ProductDTO product=new ProductDTO();product.setEstado(true);product.setPrecio(10.0);when(products.findById(anyLong())).thenReturn(product);
  java.util.concurrent.atomic.AtomicReference<Order> saved=new java.util.concurrent.atomic.AtomicReference<>();
  when(orders.saveAndFlush(any(Order.class))).thenAnswer(inv->{Order o=inv.getArgument(0);if(o.getOrderId()==null)o.setOrderId(92L);saved.set(o);return o;});
  when(orders.findByEstado("COMPENSACION_PENDIENTE")).thenAnswer(inv->saved.get()!=null&&"COMPENSACION_PENDIENTE".equals(saved.get().getEstado())?List.of(saved.get()):List.of());
  when(orders.findByEstadoAndSagaUpdatedAtBefore(eq("PREPARANDO"),any())).thenReturn(List.of());
  when(inventory.reservar(92L,2L,1L)).thenReturn(new Object());when(inventory.reservar(92L,3L,1L)).thenThrow(new RuntimeException("inventory unavailable"));
  when(inventory.liberar(92L,2L)).thenThrow(new RuntimeException("timeout")).thenReturn(new Object());when(inventory.liberar(92L,3L)).thenReturn(new Object());
  org.mockito.Mockito.doThrow(new RuntimeException("promotion timeout")).doNothing().when(promotions).release(92L);
  OrderServiceImpl service=new OrderServiceImpl(orders,inventory,users,products,promotions);OrderDTO request=new OrderDTO();request.setIdUsuario(7L);request.setDetails(List.of(line(2L),line(3L)));
  assertThrows(OrderException.class,()->service.save(request));assertEquals("COMPENSACION_PENDIENTE",saved.get().getEstado());
  service.retryCompensations();assertEquals("CANCELADA",saved.get().getEstado());verify(inventory,times(2)).liberar(92L,2L);verify(inventory,times(2)).liberar(92L,3L);verify(promotions,times(2)).release(92L);
 }

 private DetailOrderDTO line(Long productId){DetailOrderDTO d=new DetailOrderDTO();d.setIdProduct(productId);d.setCantidad(1L);return d;}
}
