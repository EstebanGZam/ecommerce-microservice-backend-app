package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Address;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.AddressDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.AddressNotFoundException;
import com.selimhorri.app.repository.AddressRepository;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private Address testAddress;
    private AddressDto testAddressDto;
    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();

        testUserDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .build();

        // Setup test address
        testAddress = Address.builder()
                .addressId(1)
                .fullAddress("123 Main Street, Apt 4B")
                .postalCode("12345")
                .city("New York")
                .user(testUser)
                .build();

        testAddressDto = AddressDto.builder()
                .addressId(1)
                .fullAddress("123 Main Street, Apt 4B")
                .postalCode("12345")
                .city("New York")
                .userDto(testUserDto)
                .build();
    }

    // Tests for findAll method
    @Test
    void findAll_WhenAddressesExist_ShouldReturnListOfAddressDtos() {
        // Given
        Address secondAddress = Address.builder()
                .addressId(2)
                .fullAddress("456 Oak Avenue")
                .postalCode("67890")
                .city("Los Angeles")
                .user(testUser)
                .build();
        
        List<Address> addresses = Arrays.asList(testAddress, secondAddress);
        when(addressRepository.findAll()).thenReturn(addresses);

        // When
        List<AddressDto> result = addressService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("123 Main Street, Apt 4B", result.get(0).getFullAddress());
        assertEquals("456 Oak Avenue", result.get(1).getFullAddress());
        verify(addressRepository, times(1)).findAll();
    }

    @Test
    void findAll_WhenNoAddressesExist_ShouldReturnEmptyList() {
        // Given
        when(addressRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<AddressDto> result = addressService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(addressRepository, times(1)).findAll();
    }

    @Test
    void findAll_WhenDuplicateAddressesExist_ShouldReturnDistinctAddresses() {
        // Given
        List<Address> addressesWithDuplicates = Arrays.asList(testAddress, testAddress);
        when(addressRepository.findAll()).thenReturn(addressesWithDuplicates);

        // When
        List<AddressDto> result = addressService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(addressRepository, times(1)).findAll();
    }

    // Tests for findById method
    @Test
    void findById_WhenAddressExists_ShouldReturnAddressDto() {
        // Given
        Integer addressId = 1;
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(testAddress));

        // When
        AddressDto result = addressService.findById(addressId);

        // Then
        assertNotNull(result);
        assertEquals(addressId, result.getAddressId());
        assertEquals("123 Main Street, Apt 4B", result.getFullAddress());
        assertEquals("12345", result.getPostalCode());
        assertEquals("New York", result.getCity());
        assertEquals("John", result.getUserDto().getFirstName());
        verify(addressRepository, times(1)).findById(addressId);
    }

    @Test
    void findById_WhenAddressDoesNotExist_ShouldThrowAddressNotFoundException() {
        // Given
        Integer addressId = 999;
        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());

        // When & Then
        AddressNotFoundException exception = assertThrows(
                AddressNotFoundException.class,
                () -> addressService.findById(addressId)
        );
        
        assertEquals("#### Address with id: 999 not found! ####", exception.getMessage());
        verify(addressRepository, times(1)).findById(addressId);
    }

    @Test
    void findById_WhenNullId_ShouldCallRepositoryWithNull() {
        // Given
        Integer addressId = null;
        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AddressNotFoundException.class, () -> addressService.findById(addressId));
        verify(addressRepository, times(1)).findById(addressId);
    }

    // Tests for save method
    @Test
    void save_WhenValidAddressDto_ShouldReturnSavedAddressDto() {
        // Given
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        // When
        AddressDto result = addressService.save(testAddressDto);

        // Then
        assertNotNull(result);
        assertEquals("123 Main Street, Apt 4B", result.getFullAddress());
        assertEquals("12345", result.getPostalCode());
        assertEquals("New York", result.getCity());
        assertEquals("John", result.getUserDto().getFirstName());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void save_WhenRepositoryFails_ShouldPropagateException() {
        // Given
        when(addressRepository.save(any(Address.class))).thenThrow(new RuntimeException("Database constraint violation"));

        // When & Then
        assertThrows(RuntimeException.class, () -> addressService.save(testAddressDto));
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void save_WhenAddressDtoWithMinimalData_ShouldSaveCorrectly() {
        // Given
        AddressDto minimalAddress = AddressDto.builder()
                .city("Miami")
                .userDto(testUserDto)
                .build();
        Address savedAddress = Address.builder()
                .addressId(1)
                .city("Miami")
                .user(testUser)
                .build();
        
        when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);

        // When
        AddressDto result = addressService.save(minimalAddress);

        // Then
        assertNotNull(result);
        assertEquals("Miami", result.getCity());
        assertNull(result.getFullAddress());
        assertNull(result.getPostalCode());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    // Tests for update method (single parameter)
    @Test
    void update_WhenValidAddressDto_ShouldReturnUpdatedAddressDto() {
        // Given
        testAddressDto.setFullAddress("789 Updated Street");
        Address updatedAddress = Address.builder()
                .addressId(1)
                .fullAddress("789 Updated Street")
                .postalCode("12345")
                .city("New York")
                .user(testUser)
                .build();
        
        when(addressRepository.save(any(Address.class))).thenReturn(updatedAddress);

        // When
        AddressDto result = addressService.update(testAddressDto);

        // Then
        assertNotNull(result);
        assertEquals("789 Updated Street", result.getFullAddress());
        assertEquals("New York", result.getCity());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void update_WhenRepositoryFails_ShouldPropagateException() {
        // Given
        when(addressRepository.save(any(Address.class))).thenThrow(new RuntimeException("Update failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> addressService.update(testAddressDto));
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    // Tests for update method (with addressId parameter)
    @Test
    void update_WhenValidAddressIdAndDto_ShouldReturnUpdatedAddressDto() {
        // Given
        Integer addressId = 1;
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        // When
        AddressDto result = addressService.update(addressId, testAddressDto);

        // Then
        assertNotNull(result);
        assertEquals("123 Main Street, Apt 4B", result.getFullAddress());
        verify(addressRepository, times(1)).findById(addressId);
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void update_WhenAddressIdDoesNotExist_ShouldThrowAddressNotFoundException() {
        // Given
        Integer addressId = 999;
        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AddressNotFoundException.class, 
                () -> addressService.update(addressId, testAddressDto));
        verify(addressRepository, times(1)).findById(addressId);
        verify(addressRepository, never()).save(any(Address.class));
    }

    // Tests for deleteById method
    @Test
    void deleteById_WhenValidAddressId_ShouldCallRepositoryDelete() {
        // Given
        Integer addressId = 1;

        // When
        addressService.deleteById(addressId);

        // Then
        verify(addressRepository, times(1)).deleteById(addressId);
    }

    @Test
    void deleteById_WhenNullAddressId_ShouldCallRepositoryWithNull() {
        // Given
        Integer addressId = null;

        // When
        addressService.deleteById(addressId);

        // Then
        verify(addressRepository, times(1)).deleteById(addressId);
    }

    @Test
    void deleteById_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        Integer addressId = 1;
        doThrow(new RuntimeException("Foreign key constraint")).when(addressRepository).deleteById(addressId);

        // When & Then
        assertThrows(RuntimeException.class, () -> addressService.deleteById(addressId));
        verify(addressRepository, times(1)).deleteById(addressId);
    }
}