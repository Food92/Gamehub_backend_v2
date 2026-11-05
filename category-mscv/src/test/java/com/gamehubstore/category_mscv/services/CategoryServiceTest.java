package com.gamehubstore.category_mscv.services;

import com.gamehubstore.category_mscv.exceptions.CategoryException;
import com.gamehubstore.category_mscv.models.Category;
import com.gamehubstore.category_mscv.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category categoryPrueba;

    @BeforeEach
    public void setUp() {
        this.categoryPrueba = new Category();
        this.categoryPrueba.setIdCategory(1L);
        this.categoryPrueba.setNombreCategory("Notebooks Gamer");
        this.categoryPrueba.setDescripcionCategory("Computadores portátiles de alto rendimiento");
        this.categoryPrueba.setEstado(Category.EstadoCategory.ACTIVO);
    }

    @Test
    @DisplayName("Debe guardar una categoría nueva exitosamente")
    public void shouldSaveCategorySuccessfully() {
        // Arrange
        when(this.categoryRepository.existsByNombreCategory(anyString())).thenReturn(false);
        when(this.categoryRepository.save(any(Category.class))).thenReturn(this.categoryPrueba);

        // Act
        Category resultado = this.categoryService.save(this.categoryPrueba);

        // Assert
        assertNotNull(resultado);
        assertEquals(Category.EstadoCategory.ACTIVO, resultado.getEstado());
        verify(this.categoryRepository, times(1)).existsByNombreCategory(anyString());
        verify(this.categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el nombre de categoría ya existe al guardar")
    public void shouldThrowExceptionWhenNameExistsOnSave() {
        // Arrange
        when(this.categoryRepository.existsByNombreCategory("Notebooks Gamer")).thenReturn(true);

        // Act & Assert
        CategoryException exception = assertThrows(CategoryException.class, () -> {
            this.categoryService.save(this.categoryPrueba);
        });

        assertEquals("Nombre de categoria ya existe", exception.getMessage());
        verify(this.categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Debe listar todas las categorías")
    public void shouldFindAllCategories() {
        // Arrange
        List<Category> listaMock = new ArrayList<>();
        listaMock.add(this.categoryPrueba);
        when(this.categoryRepository.findAll()).thenReturn(listaMock);

        // Act
        List<Category> resultado = this.categoryService.findAll();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(this.categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe buscar una categoría por ID exitosamente")
    public void shouldFindCategoryByIdSuccessfully() {
        // Arrange
        when(this.categoryRepository.findById(1L)).thenReturn(Optional.of(this.categoryPrueba));

        // Act
        Category resultado = this.categoryService.findById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdCategory());
        assertEquals("Notebooks Gamer", resultado.getNombreCategory());
        verify(this.categoryRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el ID no existe en findById")
    public void shouldThrowExceptionWhenIdNotFound() {
        // Arrange
        when(this.categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        CategoryException exception = assertThrows(CategoryException.class, () -> {
            this.categoryService.findById(99L);
        });

        assertEquals("Nombre de categoria no encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Debe actualizar el nombre y la descripción de una categoría")
    public void shouldUpdateCategoryFieldsSuccessfully() {
        // Arrange
        when(this.categoryRepository.findById(1L)).thenReturn(Optional.of(this.categoryPrueba));

        Category categoryActualizada = new Category();
        categoryActualizada.setIdCategory(1L);
        categoryActualizada.setNombreCategory("Notebooks Premium");
        categoryActualizada.setDescripcionCategory("Descripción modificada");
        categoryActualizada.setEstado(Category.EstadoCategory.ACTIVO);

        when(this.categoryRepository.save(any(Category.class))).thenReturn(categoryActualizada);

        // Act
        Category resultado = this.categoryService.update(1L, "Notebooks Premium", "Descripción modificada");

        // Assert
        assertNotNull(resultado);
        assertEquals("Notebooks Premium", resultado.getNombreCategory());
        assertEquals("Descripción modificada", resultado.getDescripcionCategory());
        verify(this.categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Debe desactivar categoría cambiando su estado a INACTIVO")
    public void shouldDesactivarCategorySuccessfully() {
        // Arrange
        when(this.categoryRepository.findById(1L)).thenReturn(Optional.of(this.categoryPrueba));
        when(this.categoryRepository.save(any(Category.class))).thenReturn(this.categoryPrueba);

        // Act
        this.categoryService.desactivar(1L);

        // Assert
        verify(this.categoryRepository, times(1)).save(argThat(category ->
                category.getEstado() == Category.EstadoCategory.INACTIVO
        ));
    }
}