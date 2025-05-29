package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.domain.Order;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.exception.wrapper.OrderNotFoundException;
import com.selimhorri.app.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private OrderDto testOrderDto;
    private Cart testCart;
    private CartDto testCartDto;

    @BeforeEach
    void setUp() {
        // Setup test data
        testCart = Cart.builder()
                .cartId(1)
                .userId(1)
                .build();

        testCartDto = CartDto.builder()
                .cartId(1)
                .userId(1)
                .build();

        testOrder = Order.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Test Order")
                .orderFee(100.0)
                .cart(testCart)
                .build();

        testOrderDto = OrderDto.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Test Order")
                .orderFee(100.0)
                .cartDto(testCartDto)
                .build();
    }

    @Test
    void findAll_WhenOrdersExist_ShouldReturnOrderDtoList() {
        // Given
        Order order1 = Order.builder()
                .orderId(1)
                .orderDate(LocalDateTime.now())
                .orderDesc("Order 1")
                .orderFee(100.0)
                .cart(testCart)
                .build();

        Order order2 = Order.builder()
                .orderId(2)
                .orderDate(LocalDateTime.now())
                .orderDesc("Order 2")
                .orderFee(200.0)
                .cart(testCart)
                .build();

        List<Order> orders = Arrays.asList(order1, order2);
        when(orderRepository.findAll()).thenReturn(orders);

        // When
        List<OrderDto> result = orderService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Order 1", result.get(0).getOrderDesc());
        assertEquals("Order 2", result.get(1).getOrderDesc());
        assertEquals(100.0, result.get(0).getOrderFee());
        assertEquals(200.0, result.get(1).getOrderFee());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void findById_WhenOrderExists_ShouldReturnOrderDto() {
        // Given
        Integer orderId = 1;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When
        OrderDto result = orderService.findById(orderId);

        // Then
        assertNotNull(result);
        assertEquals(testOrder.getOrderId(), result.getOrderId());
        assertEquals(testOrder.getOrderDesc(), result.getOrderDesc());
        assertEquals(testOrder.getOrderFee(), result.getOrderFee());
        assertEquals(testOrder.getCart().getCartId(), result.getCartDto().getCartId());

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void findById_WhenOrderNotExists_ShouldThrowOrderNotFoundException() {
        // Given
        Integer orderId = 999;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.findById(orderId));

        assertEquals("Order with id: 999 not found", exception.getMessage());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void save_WhenValidOrderDto_ShouldReturnSavedOrderDto() {
        // Given
        OrderDto inputOrderDto = OrderDto.builder()
                .orderDesc("New Order")
                .orderFee(150.0)
                .cartDto(testCartDto)
                .build();

        Order savedOrder = Order.builder()
                .orderId(3)
                .orderDate(LocalDateTime.now())
                .orderDesc("New Order")
                .orderFee(150.0)
                .cart(testCart)
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        OrderDto result = orderService.save(inputOrderDto);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getOrderId());
        assertEquals("New Order", result.getOrderDesc());
        assertEquals(150.0, result.getOrderFee());
        assertEquals(1, result.getCartDto().getCartId());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void deleteById_WhenOrderExists_ShouldDeleteOrder() {
        // Given
        Integer orderId = 1;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        doNothing().when(orderRepository).delete(any(Order.class));

        // When
        assertDoesNotThrow(() -> orderService.deleteById(orderId));

        // Then
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).delete(any(Order.class));
    }
}