package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.domain.OrderItem;
import com.selimhorri.app.domain.id.OrderItemId;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.OrderItemNotFoundException;
import com.selimhorri.app.repository.OrderItemRepository;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

        @Mock
        private OrderItemRepository orderItemRepository;

        @Mock
        private RestTemplate restTemplate;

        @InjectMocks
        private OrderItemServiceImpl orderItemService;

        private OrderItem testOrderItem;
        private OrderItemDto testOrderItemDto;
        private OrderItemId testOrderItemId;
        private ProductDto testProductDto;
        private OrderDto testOrderDto;

        @BeforeEach
        void setUp() {
                testOrderItemId = new OrderItemId(1, 1); // productId=1, orderId=1

                testProductDto = ProductDto.builder()
                                .productId(1)
                                .productTitle("Test Product")
                                .sku("TEST-SKU-001")
                                .priceUnit(25.99)
                                .quantity(100)
                                .build();

                testOrderDto = OrderDto.builder()
                                .orderId(1)
                                .orderDate(LocalDateTime.now())
                                .orderDesc("Test Order")
                                .orderFee(150.0)
                                .build();

                testOrderItem = OrderItem.builder()
                                .productId(1)
                                .orderId(1)
                                .orderedQuantity(3)
                                .build();

                testOrderItemDto = OrderItemDto.builder()
                                .productId(1)
                                .orderId(1)
                                .orderedQuantity(3)
                                .productDto(ProductDto.builder().productId(1).build())
                                .orderDto(OrderDto.builder().orderId(1).build())
                                .build();
        }

        @Test
        void findAll_WhenOrderItemsExist_ShouldReturnOrderItemDtoListWithIntegratedData() {
                // Given
                OrderItem orderItem1 = OrderItem.builder()
                                .productId(1)
                                .orderId(1)
                                .orderedQuantity(2)
                                .build();

                OrderItem orderItem2 = OrderItem.builder()
                                .productId(2)
                                .orderId(1)
                                .orderedQuantity(1)
                                .build();

                List<OrderItem> orderItems = Arrays.asList(orderItem1, orderItem2);

                ProductDto product1 = ProductDto.builder()
                                .productId(1)
                                .productTitle("Product 1")
                                .priceUnit(50.0)
                                .build();

                ProductDto product2 = ProductDto.builder()
                                .productId(2)
                                .productTitle("Product 2")
                                .priceUnit(75.0)
                                .build();

                OrderDto order1 = OrderDto.builder()
                                .orderId(1)
                                .orderDesc("Test Order 1")
                                .orderFee(175.0)
                                .build();

                when(orderItemRepository.findAll()).thenReturn(orderItems);
                when(restTemplate.getForObject(contains("products/1"), eq(ProductDto.class))).thenReturn(product1);
                when(restTemplate.getForObject(contains("products/2"), eq(ProductDto.class))).thenReturn(product2);
                when(restTemplate.getForObject(contains("orders/1"), eq(OrderDto.class))).thenReturn(order1);

                // When
                List<OrderItemDto> result = orderItemService.findAll();

                // Then
                assertNotNull(result);
                assertEquals(2, result.size());

                // Verify first order item
                assertEquals(1, result.get(0).getProductId());
                assertEquals(1, result.get(0).getOrderId());
                assertEquals(2, result.get(0).getOrderedQuantity());
                assertEquals("Product 1", result.get(0).getProductDto().getProductTitle());
                assertEquals("Test Order 1", result.get(0).getOrderDto().getOrderDesc());

                // Verify second order item
                assertEquals(2, result.get(1).getProductId());
                assertEquals(1, result.get(1).getOrderedQuantity());
                assertEquals("Product 2", result.get(1).getProductDto().getProductTitle());

                verify(orderItemRepository, times(1)).findAll();
                verify(restTemplate, times(2)).getForObject(contains("products/"), eq(ProductDto.class));
                verify(restTemplate, times(2)).getForObject(contains("orders/"), eq(OrderDto.class));
        }

        @Test
        void findById_WhenOrderItemExists_ShouldReturnOrderItemDtoWithIntegratedData() {
                // Given
                when(orderItemRepository.findById(null)).thenReturn(Optional.of(testOrderItem));
                when(restTemplate.getForObject(contains("products/1"), eq(ProductDto.class)))
                                .thenReturn(testProductDto);
                when(restTemplate.getForObject(contains("orders/1"), eq(OrderDto.class))).thenReturn(testOrderDto);

                // When
                OrderItemDto result = orderItemService.findById(testOrderItemId);

                // Then
                assertNotNull(result);
                assertEquals(testOrderItem.getProductId(), result.getProductId());
                assertEquals(testOrderItem.getOrderId(), result.getOrderId());
                assertEquals(testOrderItem.getOrderedQuantity(), result.getOrderedQuantity());
                assertEquals(testProductDto.getProductTitle(), result.getProductDto().getProductTitle());
                assertEquals(testProductDto.getSku(), result.getProductDto().getSku());
                assertEquals(testOrderDto.getOrderDesc(), result.getOrderDto().getOrderDesc());
                assertEquals(testOrderDto.getOrderFee(), result.getOrderDto().getOrderFee());

                verify(orderItemRepository, times(1)).findById(null);
                verify(restTemplate, times(1)).getForObject(contains("products/"), eq(ProductDto.class));
                verify(restTemplate, times(1)).getForObject(contains("orders/"), eq(OrderDto.class));
        }

        @Test
        void findById_WhenOrderItemNotExists_ShouldThrowOrderItemNotFoundException() {
                // Given
                OrderItemId nonExistentId = new OrderItemId(999, 999);
                when(orderItemRepository.findById(null)).thenReturn(Optional.empty());

                // When & Then
                OrderItemNotFoundException exception = assertThrows(
                                OrderItemNotFoundException.class,
                                () -> orderItemService.findById(nonExistentId));

                assertTrue(exception.getMessage().contains("OrderItem with id:"));
                assertTrue(exception.getMessage().contains("not found"));
                verify(orderItemRepository, times(1)).findById(null);
                verify(restTemplate, never()).getForObject(anyString(), eq(ProductDto.class));
                verify(restTemplate, never()).getForObject(anyString(), eq(OrderDto.class));
        }

        @Test
        void save_WhenValidOrderItemDto_ShouldReturnSavedOrderItemDto() {
                // Given
                OrderItemDto inputOrderItemDto = OrderItemDto.builder()
                                .productId(2)
                                .orderId(2)
                                .orderedQuantity(5)
                                .productDto(ProductDto.builder().productId(2).build())
                                .orderDto(OrderDto.builder().orderId(2).build())
                                .build();

                OrderItem savedOrderItem = OrderItem.builder()
                                .productId(2)
                                .orderId(2)
                                .orderedQuantity(5)
                                .build();

                when(orderItemRepository.save(any(OrderItem.class))).thenReturn(savedOrderItem);

                // When
                OrderItemDto result = orderItemService.save(inputOrderItemDto);

                // Then
                assertNotNull(result);
                assertEquals(2, result.getProductId());
                assertEquals(2, result.getOrderId());
                assertEquals(5, result.getOrderedQuantity());
                assertEquals(2, result.getProductDto().getProductId());
                assertEquals(2, result.getOrderDto().getOrderId());

                verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        }

        @Test
        void update_WhenValidOrderItemDto_ShouldReturnUpdatedOrderItemDto() {
                // Given
                OrderItemDto updateOrderItemDto = OrderItemDto.builder()
                                .productId(1)
                                .orderId(1)
                                .orderedQuantity(10) // Updated quantity
                                .productDto(ProductDto.builder().productId(1).build())
                                .orderDto(OrderDto.builder().orderId(1).build())
                                .build();

                OrderItem updatedOrderItem = OrderItem.builder()
                                .productId(1)
                                .orderId(1)
                                .orderedQuantity(10)
                                .build();

                when(orderItemRepository.save(any(OrderItem.class))).thenReturn(updatedOrderItem);

                // When
                OrderItemDto result = orderItemService.update(updateOrderItemDto);

                // Then
                assertNotNull(result);
                assertEquals(1, result.getProductId());
                assertEquals(1, result.getOrderId());
                assertEquals(10, result.getOrderedQuantity()); // Verify updated quantity
                assertEquals(1, result.getProductDto().getProductId());
                assertEquals(1, result.getOrderDto().getOrderId());

                verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        }

        @Test
        void deleteById_WhenValidOrderItemId_ShouldCallRepositoryDelete() {
                // Given
                OrderItemId orderItemId = new OrderItemId(1, 1);
                doNothing().when(orderItemRepository).deleteById(orderItemId);

                // When
                assertDoesNotThrow(() -> orderItemService.deleteById(orderItemId));

                // Then
                verify(orderItemRepository, times(1)).deleteById(orderItemId);
        }

        @Test
        void findAll_WhenMultipleQuantitiesExist_ShouldReturnCorrectQuantities() {
                // Given
                OrderItem highQuantityItem = OrderItem.builder()
                                .productId(1)
                                .orderId(1)
                                .orderedQuantity(100)
                                .build();

                OrderItem lowQuantityItem = OrderItem.builder()
                                .productId(2)
                                .orderId(1)
                                .orderedQuantity(1)
                                .build();

                List<OrderItem> orderItems = Arrays.asList(highQuantityItem, lowQuantityItem);

                ProductDto expensiveProduct = ProductDto.builder()
                                .productId(1)
                                .productTitle("Expensive Item")
                                .priceUnit(500.0)
                                .build();

                ProductDto cheapProduct = ProductDto.builder()
                                .productId(2)
                                .productTitle("Cheap Item")
                                .priceUnit(5.0)
                                .build();

                OrderDto bulkOrder = OrderDto.builder()
                                .orderId(1)
                                .orderDesc("Bulk Order")
                                .orderFee(50005.0)
                                .build();

                when(orderItemRepository.findAll()).thenReturn(orderItems);
                when(restTemplate.getForObject(contains("products/1"), eq(ProductDto.class)))
                                .thenReturn(expensiveProduct);
                when(restTemplate.getForObject(contains("products/2"), eq(ProductDto.class))).thenReturn(cheapProduct);
                when(restTemplate.getForObject(contains("orders/1"), eq(OrderDto.class))).thenReturn(bulkOrder);

                // When
                List<OrderItemDto> result = orderItemService.findAll();

                // Then
                assertNotNull(result);
                assertEquals(2, result.size());

                // Verify high quantity item
                assertEquals(100, result.get(0).getOrderedQuantity());
                assertEquals("Expensive Item", result.get(0).getProductDto().getProductTitle());
                assertEquals(500.0, result.get(0).getProductDto().getPriceUnit());

                // Verify low quantity item
                assertEquals(1, result.get(1).getOrderedQuantity());
                assertEquals("Cheap Item", result.get(1).getProductDto().getProductTitle());
                assertEquals(5.0, result.get(1).getProductDto().getPriceUnit());

                // Verify order data integration
                assertEquals("Bulk Order", result.get(0).getOrderDto().getOrderDesc());
                assertEquals(50005.0, result.get(0).getOrderDto().getOrderFee());

                verify(orderItemRepository, times(1)).findAll();
        }

        @Test
        void save_WhenOrderItemWithCompositeKey_ShouldHandleCorrectly() {
                // Given
                OrderItemDto compositeKeyDto = OrderItemDto.builder()
                                .productId(5)
                                .orderId(3)
                                .orderedQuantity(7)
                                .productDto(ProductDto.builder().productId(5).build())
                                .orderDto(OrderDto.builder().orderId(3).build())
                                .build();

                OrderItem savedWithCompositeKey = OrderItem.builder()
                                .productId(5)
                                .orderId(3)
                                .orderedQuantity(7)
                                .build();

                when(orderItemRepository.save(any(OrderItem.class))).thenReturn(savedWithCompositeKey);

                // When
                OrderItemDto result = orderItemService.save(compositeKeyDto);

                // Then
                assertNotNull(result);
                assertEquals(5, result.getProductId());
                assertEquals(3, result.getOrderId());
                assertEquals(7, result.getOrderedQuantity());

                // Verify composite key handling
                assertEquals(5, result.getProductDto().getProductId());
                assertEquals(3, result.getOrderDto().getOrderId());

                verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        }
}