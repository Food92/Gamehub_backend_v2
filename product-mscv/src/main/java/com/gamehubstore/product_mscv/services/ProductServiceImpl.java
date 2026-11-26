package com.gamehubstore.product_mscv.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gamehubstore.product_mscv.client.CategoryClient;
import com.gamehubstore.product_mscv.exceptions.ProductException;
import com.gamehubstore.product_mscv.models.Product;
import com.gamehubstore.product_mscv.models.dtos.CategoryDTO;
import com.gamehubstore.product_mscv.models.dtos.ProductDTO;
import com.gamehubstore.product_mscv.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired(required = false)
    private FirestoreSyncService firestoreSyncService;




    @Transactional(readOnly = true)
    @Override
    public List<ProductDTO> findAll() {
        return productRepository.findAll().stream().map(p -> {
            ProductDTO dto = new ProductDTO();
            dto.setIdProduct(p.getIdProduct());
            dto.setNombreProduct(p.getNombreProduct());
            dto.setModelo(p.getModelo());
            dto.setPrecio(p.getPrecio());
            dto.setMarca(p.getMarca());
            dto.setDescripcion(p.getDescripcion());
            dto.setEstado(p.getEstado());
            dto.setIdCategory(p.getIdCategory());
            dto.setImagenUrl(p.getImagenUrl());
            dto.setPrecioOferta(p.getPrecioOferta());
            dto.setEnOferta(p.getEnOferta());
            return dto;
        }).toList();
    }





    @Transactional(readOnly = true)
    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductException("El producto con el id: " + id + " no existe"));
    }



    @Transactional
    @Override
    public Product save(Product product) {
        log.info("operation={} started", "save");
        // Validar duplicado por nombre
        if (productRepository.existsByNombreProduct(product.getNombreProduct())) {
            throw new ProductException("Ya existe un producto con el nombre: " + product.getNombreProduct());
        }

        // Validar precio
        if (product.getPrecio() == null || product.getPrecio() <= 0) {
            throw new ProductException("El precio del producto debe ser mayor a 0");
        }

        // Validar categoría via CategoryClient
        if (product.getIdCategory() == null) {
            throw new ProductException("El producto debe tener una categoría asignada");
        }
        CategoryDTO  DTO = categoryClient.findById(product.getIdCategory());
        if(DTO==null) {
            throw new ProductException("La categoria no existe en el sistema");
        }
        if("INACTIVO".equals(DTO.getEstado())) {
            throw new ProductException("La categoria no se puede estar inactiva");
        }

        Product saved = productRepository.save(product);
        if (firestoreSyncService != null) firestoreSyncService.sincronizarProducto(saved);
        return saved;
    }




    @Transactional
    @Override
    public Product update(Product product, Long id) {
        log.info("operation={} started", "update");
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("El producto con id " + id + " no existe"));

        if (product.getPrecio() != null && product.getPrecio() <= 0) {
            throw new ProductException("El precio debe ser mayor a cero");
        }
        existing.setNombreProduct(product.getNombreProduct());
        existing.setMarca(product.getMarca());
        existing.setModelo(product.getModelo());
        existing.setPrecio(product.getPrecio());
        if (product.getIdCategory() != null) {
            existing.setIdCategory(product.getIdCategory());
        }
        existing.setDescripcion(product.getDescripcion());
        existing.setEstado(product.getEstado());
        if (product.getImagenUrl() != null) {
            existing.setImagenUrl(product.getImagenUrl());
        }
        existing.setPrecioOferta(product.getPrecioOferta());
        existing.setEnOferta(product.getEnOferta());
        Product saved = productRepository.save(existing);
        if (firestoreSyncService != null) firestoreSyncService.sincronizarProducto(saved);
        return saved;
    }




    @Transactional
    @Override
    public void delete(Long id) {
        log.info("operation={} started", "delete");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("El producto con id " + id + " no existe"));
        productRepository.delete(product);
        if (firestoreSyncService != null) firestoreSyncService.eliminarProducto(id);
    }





    @Transactional
    @Override
    public void desactivar(Long id) {
        log.info("operation={} started", "desactivar");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("El producto con id " + id + " no existe"));
        product.setEstado(false);
        Product saved = productRepository.save(product);
        if (firestoreSyncService != null) firestoreSyncService.sincronizarProducto(saved);
    }





    @Transactional(readOnly = true)
    @Override
    public List<Product> findByMarca(String marca) {
        return productRepository.findByMarca(marca);
    }




    @Transactional(readOnly = true)
    @Override
    public List<Product> findByIdCategory(Long idCategory) {
        return productRepository.findByIdCategory(idCategory);
    }




    @Transactional(readOnly = true)
    @Override
    public List<Product> findByEstado(Boolean estado) {
        return productRepository.findByEstado(estado);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Product> buscarCatalogo(Long categoriaId, String buscar, Boolean soloOfertas, String orden) {
        String query = (buscar == null || buscar.isBlank()) ? null : buscar.trim().toLowerCase(Locale.ROOT);
        Comparator<Product> comparator = switch (orden == null ? "" : orden.trim().toLowerCase(Locale.ROOT)) {
            case "precio_asc" -> Comparator.comparingDouble(ProductServiceImpl::precioVigente);
            case "precio_desc" -> Comparator.comparingDouble(ProductServiceImpl::precioVigente).reversed();
            case "nombre" -> Comparator.comparing(p -> p.getNombreProduct() == null ? "" : p.getNombreProduct().toLowerCase(Locale.ROOT));
            default -> Comparator.comparingLong(p -> p.getIdProduct() == null ? 0L : p.getIdProduct());
        };
        return productRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getEstado()))
                .filter(p -> categoriaId == null || categoriaId.equals(p.getIdCategory()))
                .filter(p -> !Boolean.TRUE.equals(soloOfertas) || Boolean.TRUE.equals(p.getEnOferta()))
                .filter(p -> query == null || matchesQuery(p, query))
                .sorted(comparator)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Product> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return productRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, Long> contarPorCategoria(boolean soloActivos) {
        return productRepository.findAll().stream()
                .filter(p -> !soloActivos || Boolean.TRUE.equals(p.getEstado()))
                .filter(p -> p.getIdCategory() != null)
                .collect(Collectors.groupingBy(Product::getIdCategory, Collectors.counting()));
    }

    @Transactional
    @Override
    public Product cambiarActivo(Long id, boolean activo) {
        log.info("operation={} started", "cambiarActivo");
        Product product = findById(id);
        product.setEstado(activo);
        Product saved = productRepository.save(product);
        if (firestoreSyncService != null) firestoreSyncService.sincronizarProducto(saved);
        return saved;
    }

    private static double precioVigente(Product p) {
        if (Boolean.TRUE.equals(p.getEnOferta()) && p.getPrecioOferta() != null) {
            return p.getPrecioOferta();
        }
        return p.getPrecio() != null ? p.getPrecio() : 0.0;
    }

    private static boolean matchesQuery(Product p, String q) {
        return containsIgnoreCase(p.getNombreProduct(), q)
                || containsIgnoreCase(p.getMarca(), q)
                || containsIgnoreCase(p.getModelo(), q)
                || containsIgnoreCase(p.getDescripcion(), q);
    }

    private static boolean containsIgnoreCase(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }
}

