package com.selimhorri.app.service.impl;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.FavouriteNotFoundException;
import com.selimhorri.app.repository.FavouriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FavouriteServiceImpl
 * Tests the core business logic of the favourite service
 */
@ExtendWith(MockitoExtension.class)
class FavouriteServiceImplTest {

    @Mock
    private FavouriteRepository favouriteRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FavouriteServiceImpl favouriteService;

    private Favourite testFavourite;
    private FavouriteDto testFavouriteDto;
    private FavouriteId testFavouriteId;
    private UserDto testUserDto;
    private ProductDto testProductDto;
    private LocalDateTime testLikeDate;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testLikeDate = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        
        testFavouriteId = new FavouriteId(1, 1, testLikeDate);
        
        testFavourite = Favourite.builder()
                .userId(1)
                .productId(1)
                .likeDate(testLikeDate)
                .build();

        testUserDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .build();

        testProductDto = ProductDto.builder()
                .productId(1)
                .productTitle("Test Product")
                .priceUnit(99.99)
                .quantity(10)
                .build();

        testFavouriteDto = FavouriteDto.builder()
                .userId(1)
                .productId(1)
                .likeDate(testLikeDate)
                .userDto(testUserDto)
                .productDto(testProductDto)
                .build();
    }

    @Test
    void findAll_WhenFavouritesExist_ShouldReturnListWithExternalData() {
        // Given
        List<Favourite> favourites = Arrays.asList(testFavourite);
        when(favouriteRepository.findAll()).thenReturn(favourites);
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/1", 
                UserDto.class))
                .thenReturn(testUserDto);
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/1", 
                ProductDto.class))
                .thenReturn(testProductDto);

        // When
        List<FavouriteDto> result = favouriteService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        FavouriteDto resultDto = result.get(0);
        assertEquals(1, resultDto.getUserId());
        assertEquals(1, resultDto.getProductId());
        assertEquals(testLikeDate, resultDto.getLikeDate());
        assertNotNull(resultDto.getUserDto());
        assertNotNull(resultDto.getProductDto());
        assertEquals("John", resultDto.getUserDto().getFirstName());
        assertEquals("Test Product", resultDto.getProductDto().getProductTitle());
        
        verify(favouriteRepository).findAll();
        verify(restTemplate, times(2)).getForObject(anyString(), any(Class.class));
    }

    @Test
    void findById_WhenFavouriteExists_ShouldReturnFavouriteWithExternalData() {
        // Given
        when(favouriteRepository.findById(testFavouriteId)).thenReturn(Optional.of(testFavourite));
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/1", 
                UserDto.class))
                .thenReturn(testUserDto);
        when(restTemplate.getForObject(
                AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/1", 
                ProductDto.class))
                .thenReturn(testProductDto);

        // When
        FavouriteDto result = favouriteService.findById(testFavouriteId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals(1, result.getProductId());
        assertEquals(testLikeDate, result.getLikeDate());
        assertNotNull(result.getUserDto());
        assertNotNull(result.getProductDto());
        assertEquals("John", result.getUserDto().getFirstName());
        assertEquals("Test Product", result.getProductDto().getProductTitle());
        
        verify(favouriteRepository).findById(testFavouriteId);
        verify(restTemplate, times(2)).getForObject(anyString(), any(Class.class));
    }

    @Test
    void findById_WhenFavouriteNotExists_ShouldThrowFavouriteNotFoundException() {
        // Given
        FavouriteId nonExistentId = new FavouriteId(999, 999, testLikeDate);
        when(favouriteRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        FavouriteNotFoundException exception = assertThrows(
                FavouriteNotFoundException.class,
                () -> favouriteService.findById(nonExistentId)
        );

        assertTrue(exception.getMessage().contains("not found"));
        assertTrue(exception.getMessage().contains(nonExistentId.toString()));
        verify(favouriteRepository).findById(nonExistentId);
        verify(restTemplate, never()).getForObject(anyString(), any(Class.class));
    }

    @Test
    void save_WhenValidFavouriteDto_ShouldReturnSavedFavouriteDto() {
        // Given
        FavouriteDto inputDto = FavouriteDto.builder()
                .userId(1)
                .productId(1)
                .likeDate(testLikeDate)
                .build();

        when(favouriteRepository.save(any(Favourite.class))).thenReturn(testFavourite);

        // When
        FavouriteDto result = favouriteService.save(inputDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals(1, result.getProductId());
        assertEquals(testLikeDate, result.getLikeDate());
        
        verify(favouriteRepository).save(any(Favourite.class));
    }

    @Test
    void deleteById_WhenValidFavouriteId_ShouldCallRepositoryDelete() {
        // Given
        FavouriteId favouriteIdToDelete = new FavouriteId(1, 1, testLikeDate);

        // When
        favouriteService.deleteById(favouriteIdToDelete);

        // Then
        verify(favouriteRepository).deleteById(favouriteIdToDelete);
    }
}