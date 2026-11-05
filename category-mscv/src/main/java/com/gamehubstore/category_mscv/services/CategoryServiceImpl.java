package com.gamehubstore.category_mscv.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gamehubstore.category_mscv.exceptions.CategoryException;
import com.gamehubstore.category_mscv.models.Category;
import com.gamehubstore.category_mscv.client.ProductClient;
import com.gamehubstore.category_mscv.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CategoryServiceImpl implements CategoryService {
    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductClient productClient;

    @Override
    public Category save(Category category) {
        log.info("operation={} started", "save");
        // Validar el nombre
        if (categoryRepository.existsByNombreCategory(category.getNombreCategory())) {
            throw new CategoryException("Nombre de categoria ya existe");
        }
        category.setEstado(Category.EstadoCategory.ACTIVO);
        return categoryRepository.save(category);
    }


    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }


    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new CategoryException("Nombre de categoria no encontrado"));
    }


    @Override
    public Category update(Long id, String nombre, String descripcion) {
        log.info("operation={} started", "update");
        Category oldCategory = findById(id);
        if (nombre != null && !nombre.isBlank()) oldCategory.setNombreCategory(nombre);
        if (descripcion != null) oldCategory.setDescripcionCategory(descripcion);
        return categoryRepository.save(oldCategory);
    }


    @Override
    public void desactivar(Long id) {
        log.info("operation={} started", "desactivar");
        Category categoria = findById(id);

        boolean tieneProductosActivos = productClient.byCategory(id).stream()
                .anyMatch(product -> Boolean.TRUE.equals(product.get("estado")));

        if (tieneProductosActivos) {
            throw new CategoryException("No se puede desactivar una categoría con productos activos");
        }

        categoria.setEstado(Category.EstadoCategory.INACTIVO);
        categoryRepository.save(categoria);
    }


}
