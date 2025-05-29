package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private ProductDto testProductDto;
    private Category testCategory;
    private CategoryDto testCategoryDto;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .imageUrl("http://example.com/electronics.jpg")
                .build();

        testCategoryDto = CategoryDto.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .imageUrl("http://example.com/electronics.jpg")
                .build();

        testProduct = Product.builder()
                .productId(1)
                .productTitle("Laptop ASUS")
                .imageUrl("http://example.com/laptop.jpg")
                .sku("ASUS-LAP-001")
                .priceUnit(999.99)
                .quantity(50)
                .category(testCategory)
                .build();

        testProductDto = ProductDto.builder()
                .productId(1)
                .productTitle("Laptop ASUS")
                .imageUrl("http://example.com/laptop.jpg")
                .sku("ASUS-LAP-001")
                .priceUnit(999.99)
                .quantity(50)
                .categoryDto(testCategoryDto)
                .build();
    }

    @Test
    void findAll_WhenProductsExist_ShouldReturnProductDtoList() {
        // Given
        Product product1 = Product.builder()
                .productId(1)
                .productTitle("Laptop ASUS")
                .sku("ASUS-LAP-001")
                .priceUnit(999.99)
                .quantity(50)
                .category(testCategory)
                .build();

        Product product2 = Product.builder()
                .productId(2)
                .productTitle("Mouse Logitech")
                .sku("LOG-MOUSE-001")
                .priceUnit(25.99)
                .quantity(200)
                .category(testCategory)
                .build();

        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<ProductDto> result = productService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify first product
        assertEquals("Laptop ASUS", result.get(0).getProductTitle());
        assertEquals("ASUS-LAP-001", result.get(0).getSku());
        assertEquals(999.99, result.get(0).getPriceUnit());
        assertEquals(50, result.get(0).getQuantity());
        assertEquals("Electronics", result.get(0).getCategoryDto().getCategoryTitle());
        
        // Verify second product
        assertEquals("Mouse Logitech", result.get(1).getProductTitle());
        assertEquals("LOG-MOUSE-001", result.get(1).getSku());
        assertEquals(25.99, result.get(1).getPriceUnit());
        assertEquals(200, result.get(1).getQuantity());
        
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void findById_WhenProductExists_ShouldReturnProductDto() {
        // Given
        Integer productId = 1;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        ProductDto result = productService.findById(productId);

        // Then
        assertNotNull(result);
        assertEquals(testProduct.getProductId(), result.getProductId());
        assertEquals(testProduct.getProductTitle(), result.getProductTitle());
        assertEquals(testProduct.getSku(), result.getSku());
        assertEquals(testProduct.getPriceUnit(), result.getPriceUnit());
        assertEquals(testProduct.getQuantity(), result.getQuantity());
        assertEquals(testProduct.getCategory().getCategoryTitle(), result.getCategoryDto().getCategoryTitle());
        
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void findById_WhenProductNotExists_ShouldThrowProductNotFoundException() {
        // Given
        Integer productId = 999;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        ProductNotFoundException exception = assertThrows(
            ProductNotFoundException.class,
            () -> productService.findById(productId)
        );
        
        assertEquals("Product with id: 999 not found", exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void save_WhenValidProductDto_ShouldReturnSavedProductDto() {
        // Given
        ProductDto inputProductDto = ProductDto.builder()
                .productTitle("New Keyboard")
                .sku("KEYB-001")
                .priceUnit(75.50)
                .quantity(30)
                .categoryDto(testCategoryDto)
                .build();

        Product savedProduct = Product.builder()
                .productId(3)
                .productTitle("New Keyboard")
                .sku("KEYB-001")
                .priceUnit(75.50)
                .quantity(30)
                .category(testCategory)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        ProductDto result = productService.save(inputProductDto);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getProductId());
        assertEquals("New Keyboard", result.getProductTitle());
        assertEquals("KEYB-001", result.getSku());
        assertEquals(75.50, result.getPriceUnit());
        assertEquals(30, result.getQuantity());
        assertEquals("Electronics", result.getCategoryDto().getCategoryTitle());
        
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void update_WhenValidProductDto_ShouldReturnUpdatedProductDto() {
        // Given
        ProductDto updateProductDto = ProductDto.builder()
                .productId(1)
                .productTitle("Updated Laptop ASUS")
                .sku("ASUS-LAP-001-V2")
                .priceUnit(1199.99)
                .quantity(25)
                .categoryDto(testCategoryDto)
                .build();

        Product updatedProduct = Product.builder()
                .productId(1)
                .productTitle("Updated Laptop ASUS")
                .sku("ASUS-LAP-001-V2")
                .priceUnit(1199.99)
                .quantity(25)
                .category(testCategory)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // When
        ProductDto result = productService.update(updateProductDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals("Updated Laptop ASUS", result.getProductTitle());
        assertEquals("ASUS-LAP-001-V2", result.getSku());
        assertEquals(1199.99, result.getPriceUnit());
        assertEquals(25, result.getQuantity());
        
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void update_WhenProductIdAndDto_ShouldUpdateCorrectProduct() {
        // Given
        Integer productId = 1;
        ProductDto updateProductDto = ProductDto.builder()
                .productTitle("Updated Product")
                .sku("UPD-001")
                .priceUnit(150.0)
                .quantity(15)
                .categoryDto(testCategoryDto)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductDto result = productService.update(productId, updateProductDto);

        // Then
        assertNotNull(result);
        assertEquals(testProduct.getProductId(), result.getProductId());
        assertEquals(testProduct.getProductTitle(), result.getProductTitle());
        
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void deleteById_WhenProductExists_ShouldDeleteProduct() {
        // Given
        Integer productId = 1;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(any(Product.class));

        // When
        assertDoesNotThrow(() -> productService.deleteById(productId));

        // Then
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).delete(any(Product.class));
    }

    @Test
    void findAll_WhenProductsWithDifferentPrices_ShouldReturnCorrectPrices() {
        // Given
        Product expensiveProduct = Product.builder()
                .productId(1)
                .productTitle("Gaming Laptop")
                .sku("GAMING-001")
                .priceUnit(2500.00)
                .quantity(10)
                .category(testCategory)
                .build();

        Product budgetProduct = Product.builder()
                .productId(2)
                .productTitle("Basic Mouse")
                .sku("MOUSE-BASIC-001")
                .priceUnit(9.99)
                .quantity(500)
                .category(testCategory)
                .build();

        List<Product> products = Arrays.asList(expensiveProduct, budgetProduct);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<ProductDto> result = productService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify expensive product
        assertEquals(2500.00, result.get(0).getPriceUnit());
        assertEquals(10, result.get(0).getQuantity());
        assertEquals("Gaming Laptop", result.get(0).getProductTitle());
        
        // Verify budget product
        assertEquals(9.99, result.get(1).getPriceUnit());
        assertEquals(500, result.get(1).getQuantity());
        assertEquals("Basic Mouse", result.get(1).getProductTitle());
        
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void save_WhenProductWithZeroStock_ShouldSaveCorrectly() {
        // Given
        ProductDto outOfStockDto = ProductDto.builder()
                .productTitle("Out of Stock Item")
                .sku("OOS-001")
                .priceUnit(199.99)
                .quantity(0)
                .categoryDto(testCategoryDto)
                .build();

        Product outOfStockProduct = Product.builder()
                .productId(4)
                .productTitle("Out of Stock Item")
                .sku("OOS-001")
                .priceUnit(199.99)
                .quantity(0)
                .category(testCategory)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(outOfStockProduct);

        // When
        ProductDto result = productService.save(outOfStockDto);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getQuantity());
        assertEquals("Out of Stock Item", result.getProductTitle());
        assertEquals(199.99, result.getPriceUnit());
        
        verify(productRepository, times(1)).save(any(Product.class));
    }
}