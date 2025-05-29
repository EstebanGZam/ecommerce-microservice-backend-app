package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.CartNotFoundException;
import com.selimhorri.app.repository.CartRepository;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private CartDto testCartDto;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        testCart = Cart.builder()
                .cartId(1)
                .userId(1)
                .build();

        testCartDto = CartDto.builder()
                .cartId(1)
                .userId(1)
                .userDto(UserDto.builder().userId(1).build())
                .build();
    }

    @Test
    void findAll_WhenCartsExist_ShouldReturnCartDtoListWithUserData() {
        // Given
        Cart cart1 = Cart.builder().cartId(1).userId(1).build();
        Cart cart2 = Cart.builder().cartId(2).userId(2).build();
        List<Cart> carts = Arrays.asList(cart1, cart2);

        UserDto user1 = UserDto.builder().userId(1).firstName("John").build();
        UserDto user2 = UserDto.builder().userId(2).firstName("Jane").build();

        when(cartRepository.findAll()).thenReturn(carts);
        when(restTemplate.getForObject(anyString(), eq(UserDto.class)))
                .thenReturn(user1)
                .thenReturn(user2);

        // When
        List<CartDto> result = cartService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getCartId());
        assertEquals(2, result.get(1).getCartId());
        assertEquals("John", result.get(0).getUserDto().getFirstName());
        assertEquals("Jane", result.get(1).getUserDto().getFirstName());
        
        verify(cartRepository, times(1)).findAll();
        verify(restTemplate, times(2)).getForObject(anyString(), eq(UserDto.class));
    }

    @Test
    void findById_WhenCartExists_ShouldReturnCartDtoWithUserData() {
        // Given
        Integer cartId = 1;
        when(cartRepository.findById(cartId)).thenReturn(Optional.of(testCart));
        when(restTemplate.getForObject(anyString(), eq(UserDto.class))).thenReturn(testUserDto);

        // When
        CartDto result = cartService.findById(cartId);

        // Then
        assertNotNull(result);
        assertEquals(testCart.getCartId(), result.getCartId());
        assertEquals(testCart.getUserId(), result.getUserId());
        assertEquals(testUserDto.getFirstName(), result.getUserDto().getFirstName());
        assertEquals(testUserDto.getEmail(), result.getUserDto().getEmail());
        
        verify(cartRepository, times(1)).findById(cartId);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(UserDto.class));
    }

    @Test
    void findById_WhenCartNotExists_ShouldThrowCartNotFoundException() {
        // Given
        Integer cartId = 999;
        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        // When & Then
        CartNotFoundException exception = assertThrows(
            CartNotFoundException.class,
            () -> cartService.findById(cartId)
        );
        
        assertEquals("Cart with id: 999 not found", exception.getMessage());
        verify(cartRepository, times(1)).findById(cartId);
        verify(restTemplate, never()).getForObject(anyString(), eq(UserDto.class));
    }

    @Test
    void save_WhenValidCartDto_ShouldReturnSavedCartDto() {
        // Given
        CartDto inputCartDto = CartDto.builder()
                .userId(2)
                .build();

        Cart savedCart = Cart.builder()
                .cartId(5)
                .userId(2)
                .build();

        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

        // When
        CartDto result = cartService.save(inputCartDto);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getCartId());
        assertEquals(2, result.getUserId());
        
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void deleteById_WhenValidCartId_ShouldCallRepositoryDelete() {
        // Given
        Integer cartId = 1;
        doNothing().when(cartRepository).deleteById(cartId);

        // When
        assertDoesNotThrow(() -> cartService.deleteById(cartId));

        // Then
        verify(cartRepository, times(1)).deleteById(cartId);
    }
}