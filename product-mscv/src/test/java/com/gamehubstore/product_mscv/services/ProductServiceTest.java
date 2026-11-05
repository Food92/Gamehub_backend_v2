package com.gamehubstore.product_mscv.services;

import com.gamehubstore.product_mscv.client.CategoryClient;
import com.gamehubstore.product_mscv.exceptions.ProductException;
import com.gamehubstore.product_mscv.models.Audit;
import com.gamehubstore.product_mscv.models.Product;
import com.gamehubstore.product_mscv.models.dtos.CategoryDTO;
import com.gamehubstore.product_mscv.models.dtos.ProductDTO;
import com.gamehubstore.product_mscv.repositories.ProductRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryClient categoryClient;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product productPreuba;
    private List<Product> productList;

    @BeforeEach
    public void setUp() {
        this.productList = new ArrayList<>();

        // Inicialización base del producto para las pruebas
        this.productPreuba = new Product();
        this.productPreuba.setIdProduct(1L);
        this.productPreuba.setNombreProduct("PlayStation 5 Slim");
        this.productPreuba.setMarca("Sony");
        this.productPreuba.setModelo("Slim Slim");
        this.productPreuba.setPrecio(499.99);
        this.productPreuba.setIdCategory(1L); // ID de categoría asignado explícitamente como Long
        this.productPreuba.setDescripcion("Consola de ultima generacion");
        this.productPreuba.setEstado(true);

        Audit auditFijo = new Audit();
        auditFijo.setCreatedAt(LocalDate.now());
        auditFijo.setUpdatedAt(null);
        this.productPreuba.setAudit(auditFijo);

        // Llenado masivo con Faker (100 productos aleatorios para simular el catálogo)
        Faker faker = new Faker(new Locale("es"));
        for (int i = 0; i < 100; i++) {
            Product product = new Product();
            product.setIdProduct(faker.number().randomNumber());
            product.setNombreProduct(faker.videoGame().title() + " " + faker.number().randomNumber());
            product.setMarca(faker.brand().watch());
            product.setModelo(faker.device().modelName()); // Corregido: .device().modelName() para evitar errores
            product.setPrecio(faker.number().randomDouble(2, 10, 1500));

            // Corrección aquí: Forzamos la obtención de un Long primitivo válido y no nulo
            long randomIdCategory = faker.number().numberBetween(1, 10);
            product.setIdCategory(randomIdCategory);

            product.setDescripcion(faker.lorem().sentence(4));
            product.setEstado(faker.bool().bool());

            Audit auditAleatorio = new Audit();
            auditAleatorio.setCreatedAt(LocalDate.now().minusDays(faker.number().numberBetween(1, 30)));
            auditAleatorio.setUpdatedAt(null);
            product.setAudit(auditAleatorio);

            this.productList.add(product);
        }
    }

    @Test
    @DisplayName("Debe listar todos los productos mapeados a ProductDTO")
    public void shouldBeListAllProducts() {
        // Arrange
        List<Product> mockProductos = new ArrayList<>(this.productList);
        mockProductos.add(this.productPreuba);
        when(this.productRepository.findAll()).thenReturn(mockProductos);

        // Act
        List<ProductDTO> result = this.productService.findAll();

        // Assert
        assertThat(result).hasSize(101);
        assertThat(result.get(100).getNombreProduct()).isEqualTo("PlayStation 5 Slim");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe buscar un producto por su id de forma correcta")
    public void shouldFindProductById() {
        // Arrange
        Long id = 1L;
        when(this.productRepository.findById(id)).thenReturn(Optional.of(this.productPreuba));

        // Act
        Product result = this.productService.findById(id);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNombreProduct()).isEqualTo("PlayStation 5 Slim");
        verify(productRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe lanzar ProductException si el producto por ID no existe")
    public void shouldNotFindProductById() {
        // Arrange
        Long id = 9999L;
        when(this.productRepository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> {
            this.productService.findById(id);
        }).isInstanceOf(ProductException.class)
                .hasMessage("El producto con el id: " + id + " no existe");

        verify(productRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debe guardar un producto de forma exitosa validando cliente OpenFeign")
    public void shouldSaveProductSuccessfully() {
        // Arrange
        CategoryDTO mockupCategory = new CategoryDTO();
        mockupCategory.setIdCategory(1L);
        mockupCategory.setNombreCategory("Consolas");
        mockupCategory.setEstado("ACTIVA");

        when(this.productRepository.existsByNombreProduct(this.productPreuba.getNombreProduct())).thenReturn(false);
        when(this.categoryClient.findById(this.productPreuba.getIdCategory())).thenReturn(mockupCategory);
        when(this.productRepository.save(this.productPreuba)).thenReturn(this.productPreuba);

        // Act
        Product result = this.productService.save(this.productPreuba);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNombreProduct()).isEqualTo("PlayStation 5 Slim");
        verify(productRepository, times(1)).existsByNombreProduct(this.productPreuba.getNombreProduct());
        verify(categoryClient, times(1)).findById(this.productPreuba.getIdCategory());
        verify(productRepository, times(1)).save(this.productPreuba);
    }

    @Test
    @DisplayName("Debe lanzar ProductException si el nombre del producto está duplicado")
    public void shouldNotSaveProductWhenNameExists() {
        // Arrange
        when(this.productRepository.existsByNombreProduct(this.productPreuba.getNombreProduct())).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> {
            this.productService.save(this.productPreuba);
        }).isInstanceOf(ProductException.class)
                .hasMessage("Ya existe un producto con el nombre: " + this.productPreuba.getNombreProduct());

        verify(productRepository, times(1)).existsByNombreProduct(this.productPreuba.getNombreProduct());
        verify(categoryClient, never()).findById(anyLong());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar ProductException al guardar un producto con categoría inactiva")
    public void shouldNotSaveProductWhenCategoryIsInactive() {
        // Arrange
        CategoryDTO mockupCategory = new CategoryDTO();
        mockupCategory.setIdCategory(1L);
        mockupCategory.setEstado("INACTIVA");

        when(this.productRepository.existsByNombreProduct(this.productPreuba.getNombreProduct())).thenReturn(false);
        when(this.categoryClient.findById(this.productPreuba.getIdCategory())).thenReturn(mockupCategory);

        // Act + Assert
        assertThatThrownBy(() -> {
            this.productService.save(this.productPreuba);
        }).isInstanceOf(ProductException.class)
                .hasMessage("La categoria no se puede estar inactiva");

        verify(categoryClient, times(1)).findById(this.productPreuba.getIdCategory());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe actualizar los datos de un producto existente")
    public void shouldUpdateProduct() {
        // Arrange
        Long id = 1L;
        Product cambios = new Product();
        cambios.setNombreProduct("PlayStation 5 Pro");
        cambios.setMarca("Sony Core");
        cambios.setModelo("Pro V1");
        cambios.setPrecio(699.99);
        cambios.setDescripcion("Consola potenciada");
        cambios.setEstado(true);

        when(this.productRepository.findById(id)).thenReturn(Optional.of(this.productPreuba));
        when(this.productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Product result = this.productService.update(cambios, id);

        // Assert
        assertThat(result.getNombreProduct()).isEqualTo("PlayStation 5 Pro");
        assertThat(result.getPrecio()).isEqualTo(699.99);
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).save(this.productPreuba);
    }

    @Test
    @DisplayName("Debe eliminar un producto por completo por su ID")
    public void shouldDeleteProduct() {
        // Arrange
        Long id = 1L;
        when(this.productRepository.findById(id)).thenReturn(Optional.of(this.productPreuba));

        // CORRECCIÓN AQUÍ: Se simula la eliminación del objeto Entidad, tal como hace tu ServiceImpl
        doNothing().when(this.productRepository).delete(this.productPreuba);

        // Act
        this.productService.delete(id);

        // Assert
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).delete(this.productPreuba);
    }

    @Test
    @DisplayName("Debe cambiar el estado del producto a inactivo (desactivación lógica)")
    public void shouldDesactivarProduct() {
        // Arrange
        Long id = 1L;
        when(this.productRepository.findById(id)).thenReturn(Optional.of(this.productPreuba));
        when(this.productRepository.save(this.productPreuba)).thenReturn(this.productPreuba);

        // Act
        this.productService.desactivar(id);

        // Assert
        assertThat(this.productPreuba.getEstado()).isFalse();
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).save(this.productPreuba);
    }

    @Test
    @DisplayName("Debe filtrar la lista de productos por Marca")
    public void shouldFindProductsByMarca() {
        // Arrange
        String marca = "Sony";
        when(this.productRepository.findByMarca(marca)).thenReturn(List.of(this.productPreuba));

        // Act
        List<Product> result = this.productService.findByMarca(marca);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMarca()).isEqualTo(marca);
        verify(productRepository, times(1)).findByMarca(marca);
    }

    @Test
    @DisplayName("Debe filtrar la lista de productos por ID de Categoría")
    public void shouldFindProductsByIdCategory() {
        // Arrange
        Long idCategory = 1L;
        when(this.productRepository.findByIdCategory(idCategory)).thenReturn(List.of(this.productPreuba));

        // Act
        List<Product> result = this.productService.findByIdCategory(idCategory);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdCategory()).isEqualTo(idCategory);
        verify(productRepository, times(1)).findByIdCategory(idCategory);
    }

    @Test
    @DisplayName("Debe filtrar la lista de productos por Estado lógico")
    public void shouldFindProductsByEstado() {
        // Arrange
        Boolean estado = true;
        when(this.productRepository.findByEstado(estado)).thenReturn(List.of(this.productPreuba));

        // Act
        List<Product> result = this.productService.findByEstado(estado);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEstado()).isTrue();
        verify(productRepository, times(1)).findByEstado(estado);
    }
}