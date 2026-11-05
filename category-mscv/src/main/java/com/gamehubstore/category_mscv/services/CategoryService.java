package com.gamehubstore.category_mscv.services;


import com.gamehubstore.category_mscv.models.Category;

import java.util.List;

public interface CategoryService {
    Category save(Category category);
    List<Category> findAll();
    Category update(Long id,String nombre, String descripcion);
    void desactivar(Long id);
    Category findById(Long id);
}
