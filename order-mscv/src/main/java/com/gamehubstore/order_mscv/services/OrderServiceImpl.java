package com.gamehubstore.order_mscv.services;

import com.gamehubstore.order_mscv.client.*;
import com.gamehubstore.order_mscv.exceptions.OrderException;
import com.gamehubstore.order_mscv.models.*;
import com.gamehubstore.order_mscv.models.dtos.*;
import com.gamehubstore.order_mscv.repositories.OrderRepository;
import feign.FeignException;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final String PREPARANDO = "PREPARANDO";
    private static final String PENDIENTE_PAGO = "PENDIENTE_PAGO";
    private static final String CONFIRMACION_PENDIENTE = "CONFIRMACION_PENDIENTE";
    private static final String COMPENSACION_PENDIENTE = "COMPENSACION_PENDIENTE";
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final PromotionClient promotionClient;

    public OrderServiceImpl(OrderRepository orders, InventoryClient inventory, UserClient users, ProductClient products, PromotionClient promotions) {
        this.orderRepository=orders; this.inventoryClient=inventory; this.userClient=users; this.productClient=products; this.promotionClient=promotions;
    }

    // No @Transactional: la transacción local solo cubre cada save del repositorio; la saga persiste su progreso entre llamadas REST.
    @Override
    public OrderDTO save(OrderDTO request) {
        log.info("Saga de creación iniciada para userId={}", request.getIdUsuario());
        validateUser(request.getIdUsuario());
        List<DetailOrder> details = request.getDetails().stream().map(line -> {
            var product = productClient.findById(line.getIdProduct());
            if (product == null || !Boolean.TRUE.equals(product.getEstado())) throw new OrderException("Producto inexistente o inactivo: " + line.getIdProduct());
            DetailOrder detail = new DetailOrder(); detail.setIdProducto(line.getIdProduct()); detail.setCantidad(Math.toIntExact(line.getCantidad()));
            detail.setPrecioUnitario(product.getPrecio()); detail.setPrecioTotal(product.getPrecio() * detail.getCantidad()); return detail;
        }).toList();
        double subtotal=details.stream().mapToDouble(DetailOrder::getPrecioTotal).sum();
        String code=request.getPromotionCode()==null||request.getPromotionCode().isBlank()?null:request.getPromotionCode().trim();
        BigDecimal discount=BigDecimal.ZERO;
        if(code!=null){
            Map<String,BigDecimal> quote=promotionClient.validate(code,BigDecimal.valueOf(subtotal));
            discount=quote.get("discount"); if(discount==null||discount.signum()<0||discount.compareTo(BigDecimal.valueOf(subtotal))>0)throw new OrderException("Promoción devolvió un descuento inválido");
        }
        Order order=new Order(); order.setUserId(request.getIdUsuario()); order.setEstado(PREPARANDO); order.setFecha(LocalDateTime.now()); order.setSagaUpdatedAt(LocalDateTime.now());
        order.setSubtotal(subtotal); order.setDescuento(discount.doubleValue()); order.setTotal(subtotal-discount.doubleValue()); order.setPromotionCode(code); order.setDetails(new ArrayList<>(details));
        order=orderRepository.saveAndFlush(order); // orderId existe antes de realizar efectos remotos.
        try {
            order.setSagaUpdatedAt(LocalDateTime.now()); orderRepository.saveAndFlush(order);
            if(code!=null) promotionClient.reserve(order.getOrderId(),code,BigDecimal.valueOf(subtotal));
            order.setSagaUpdatedAt(LocalDateTime.now()); orderRepository.saveAndFlush(order);
            for(DetailOrder detail:order.getDetails()) inventoryClient.reservar(order.getOrderId(),detail.getIdProducto(),detail.getCantidad().longValue());
            order.setEstado(PENDIENTE_PAGO); order.setSagaUpdatedAt(LocalDateTime.now());
            order=orderRepository.saveAndFlush(order);
            return toDto(order);
        } catch (RuntimeException failure) {
            log.error("Saga de orden {} falló; iniciando compensación",order.getOrderId(),failure);
            order.setEstado(COMPENSACION_PENDIENTE); order.setCompensationTarget("CANCELADA"); order.setSagaUpdatedAt(LocalDateTime.now()); orderRepository.saveAndFlush(order);
            boolean compensated=compensate(order);
            if(compensated){order.setEstado("CANCELADA");order.setCompensationTarget(null);order.setSagaUpdatedAt(LocalDateTime.now());} else log.error("Orden {} requiere reintento de compensación",order.getOrderId());
            orderRepository.saveAndFlush(order);
            throw new OrderException("No se pudo completar la orden "+order.getOrderId()+"; estado="+order.getEstado());
        }
    }

    private void validateUser(Long userId) {
        try { UserClientDTO user=userClient.getUserById(userId); if(user==null)throw new OrderException("El usuario no existe"); if(Boolean.FALSE.equals(user.getEstado()))throw new OrderException("Un usuario inactivo no puede comprar"); }
        catch(FeignException.NotFound ex){throw new OrderException("El usuario comprador no existe");}
    }

    private boolean compensate(Order order) {
        boolean success=true;
        for(DetailOrder detail:order.getDetails()) try { inventoryClient.liberar(order.getOrderId(),detail.getIdProducto()); }
        catch(RuntimeException ex){success=false;log.error("No se pudo liberar stock orderId={} productId={}",order.getOrderId(),detail.getIdProducto(),ex);}
        try { promotionClient.release(order.getOrderId()); }
        catch(RuntimeException ex){success=false;log.error("No se pudo liberar cupón orderId={}",order.getOrderId(),ex);}
        return success;
    }

    @Scheduled(fixedDelayString="${saga.compensation.retry-ms:30000}")
    public void retryCompensations() {
        List<Order> pending=new ArrayList<>(orderRepository.findByEstado(COMPENSACION_PENDIENTE));
        pending.addAll(orderRepository.findByEstadoAndSagaUpdatedAtBefore(PREPARANDO,LocalDateTime.now().minusMinutes(5)));
        for(Order order:pending){
            if(PREPARANDO.equals(order.getEstado())){order.setEstado(COMPENSACION_PENDIENTE);order.setCompensationTarget("CANCELADA");order.setSagaUpdatedAt(LocalDateTime.now());orderRepository.saveAndFlush(order);}
            if(compensate(order)){order.setEstado(order.getCompensationTarget()==null?"CANCELADA":order.getCompensationTarget());order.setCompensationTarget(null);order.setSagaUpdatedAt(LocalDateTime.now());orderRepository.saveAndFlush(order);log.info("Compensación completada orderId={}",order.getOrderId());}
        }
    }

    @Override @Transactional public Order update(OrderDTO dto,Long id){Order order=findById(id);if(!PENDIENTE_PAGO.equals(order.getEstado()))throw new IllegalStateException("Solo se puede editar una orden pendiente de pago");order.setTotal(dto.getTotal());order.setSubtotal(dto.getTotal());return orderRepository.save(order);}
    @Override @Transactional(readOnly=true) public Order findById(Long id){return orderRepository.findById(id).orElseThrow(()->new OrderException("Orden no encontrada: "+id));}
    @Override @Transactional(readOnly=true) public List<Order> findAll(){return orderRepository.findAll();}
    @Override @Transactional(readOnly=true) public List<Order> findByUserId(Long id){return orderRepository.findByUserId(id);}
    @Override @Transactional(readOnly=true) public List<Order> findByEstado(String estado){return orderRepository.findByEstado(estado.toUpperCase(Locale.ROOT));}

    @Override
    public Order cancelOrder(Long id) {
        Order order=findById(id); if("CANCELADA".equals(order.getEstado()))return order;
        if("PAGADA".equals(order.getEstado())||"EN_DESPACHO".equals(order.getEstado())||"ENTREGADA".equals(order.getEstado()))throw new IllegalStateException("No se puede cancelar una orden pagada o despachada por este flujo");
        order.setEstado(COMPENSACION_PENDIENTE);order.setCompensationTarget("CANCELADA");order.setSagaUpdatedAt(LocalDateTime.now());orderRepository.saveAndFlush(order);
        if(compensate(order)){order.setEstado("CANCELADA");order.setCompensationTarget(null);order.setSagaUpdatedAt(LocalDateTime.now());}
        return orderRepository.saveAndFlush(order);
    }

    @Override @Transactional(readOnly=true) public OrderDTO findByIdDTO(Long id){return toDto(findById(id));}

    @Override
    public OrderDTO updateEstado(Long id,String requested) {
        String target=requested.toUpperCase(Locale.ROOT); Order order=findById(id);
        if(target.equals(order.getEstado()))return toDto(order);
        if("PAGADA".equals(target)){
            if(!PENDIENTE_PAGO.equals(order.getEstado())&&!CONFIRMACION_PENDIENTE.equals(order.getEstado()))throw new IllegalStateException("La orden no está lista para confirmarse como pagada");
            order.setEstado(CONFIRMACION_PENDIENTE);order.setSagaUpdatedAt(LocalDateTime.now());orderRepository.saveAndFlush(order);
            try {
                for(DetailOrder detail:order.getDetails())inventoryClient.confirmar(order.getOrderId(),detail.getIdProducto());
                promotionClient.confirm(order.getOrderId());
                order.setEstado("PAGADA");order.setCompensationTarget(null);order.setSagaUpdatedAt(LocalDateTime.now());return toDto(orderRepository.saveAndFlush(order));
            } catch(RuntimeException failure){log.error("Confirmación de pago pendiente; los commits remotos son idempotentes. orderId={}",id,failure);throw new OrderException("Confirmación pendiente para orden "+id+"; reintenta el pago para completar la saga");}
        }
        if("RECHAZADA".equals(target)||"CANCELADA".equals(target)){
            if("PAGADA".equals(order.getEstado()))throw new IllegalStateException("Una orden pagada requiere un flujo de reembolso explícito");
            order.setEstado(COMPENSACION_PENDIENTE);order.setCompensationTarget(target);order.setSagaUpdatedAt(LocalDateTime.now());orderRepository.saveAndFlush(order);
            if(compensate(order)){order.setEstado(target);order.setCompensationTarget(null);order.setSagaUpdatedAt(LocalDateTime.now());}
            return toDto(orderRepository.saveAndFlush(order));
        }
        order.setEstado(target);order.setSagaUpdatedAt(LocalDateTime.now());return toDto(orderRepository.saveAndFlush(order));
    }

    private OrderDTO toDto(Order order){OrderDTO dto=new OrderDTO();dto.setOrderId(order.getOrderId());dto.setIdUsuario(order.getUserId());dto.setSubtotal(order.getSubtotal());dto.setDescuento(order.getDescuento());dto.setTotal(order.getTotal());dto.setEstado(order.getEstado());dto.setFechaCreacion(order.getFecha());dto.setPromotionCode(order.getPromotionCode());dto.setDetails(order.getDetails().stream().map(d->{DetailOrderDTO line=new DetailOrderDTO();line.setIdProduct(d.getIdProducto());line.setCantidad(d.getCantidad().longValue());line.setPrecioUnitario(d.getPrecioUnitario());line.setPrecioTotal(d.getPrecioTotal());return line;}).toList());return dto;}
}
