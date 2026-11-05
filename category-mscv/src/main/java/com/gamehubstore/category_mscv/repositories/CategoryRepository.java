package com.gamehubstore.category_mscv.repositories;

import com.gamehubstore.category_mscv.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNombreCategory(String nombreCategory);
    boolean existsByNombreCategory(String nombreCategory);

}
