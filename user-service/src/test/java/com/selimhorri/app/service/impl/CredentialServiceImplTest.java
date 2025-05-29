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

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.CredentialNotFoundException;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.repository.CredentialRepository;

@ExtendWith(MockitoExtension.class)
class CredentialServiceImplTest {

    @Mock
    private CredentialRepository credentialRepository;

    @InjectMocks
    private CredentialServiceImpl credentialService;

    private Credential testCredential;
    private CredentialDto testCredentialDto;
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

        // Setup test credential
        testCredential = Credential.builder()
                .credentialId(1)
                .username("testuser")
                .password("$2a$10$hashedpassword")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .user(testUser)
                .build();

        testCredentialDto = CredentialDto.builder()
                .credentialId(1)
                .username("testuser")
                .password("$2a$10$hashedpassword")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .userDto(testUserDto)
                .build();
    }

    // Tests for findAll method
    @Test
    void findAll_WhenCredentialsExist_ShouldReturnListOfCredentialDtos() {
        // Given
        Credential secondCredential = Credential.builder()
                .credentialId(2)
                .username("adminuser")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_ADMIN)
                .isEnabled(true)
                .user(User.builder().userId(2).firstName("Admin").build())
                .build();
        
        List<Credential> credentials = Arrays.asList(testCredential, secondCredential);
        when(credentialRepository.findAll()).thenReturn(credentials);

        // When
        List<CredentialDto> result = credentialService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("adminuser", result.get(1).getUsername());
        verify(credentialRepository, times(1)).findAll();
    }

    @Test
    void findAll_WhenNoCredentialsExist_ShouldReturnEmptyList() {
        // Given
        when(credentialRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<CredentialDto> result = credentialService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(credentialRepository, times(1)).findAll();
    }

    @Test
    void findAll_WhenDuplicateCredentialsExist_ShouldReturnDistinctCredentials() {
        // Given
        List<Credential> credentialsWithDuplicates = Arrays.asList(testCredential, testCredential);
        when(credentialRepository.findAll()).thenReturn(credentialsWithDuplicates);

        // When
        List<CredentialDto> result = credentialService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(credentialRepository, times(1)).findAll();
    }

    // Tests for findById method
    @Test
    void findById_WhenCredentialExists_ShouldReturnCredentialDto() {
        // Given
        Integer credentialId = 1;
        when(credentialRepository.findById(credentialId)).thenReturn(Optional.of(testCredential));

        // When
        CredentialDto result = credentialService.findById(credentialId);

        // Then
        assertNotNull(result);
        assertEquals(credentialId, result.getCredentialId());
        assertEquals("testuser", result.getUsername());
        assertEquals(RoleBasedAuthority.ROLE_USER, result.getRoleBasedAuthority());
        assertTrue(result.getIsEnabled());
        verify(credentialRepository, times(1)).findById(credentialId);
    }

    @Test
    void findById_WhenCredentialDoesNotExist_ShouldThrowCredentialNotFoundException() {
        // Given
        Integer credentialId = 999;
        when(credentialRepository.findById(credentialId)).thenReturn(Optional.empty());

        // When & Then
        CredentialNotFoundException exception = assertThrows(
                CredentialNotFoundException.class,
                () -> credentialService.findById(credentialId)
        );
        
        assertEquals("#### Credential with id: 999 not found! ####", exception.getMessage());
        verify(credentialRepository, times(1)).findById(credentialId);
    }

    @Test
    void findById_WhenNullId_ShouldCallRepositoryWithNull() {
        // Given
        Integer credentialId = null;
        when(credentialRepository.findById(credentialId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CredentialNotFoundException.class, () -> credentialService.findById(credentialId));
        verify(credentialRepository, times(1)).findById(credentialId);
    }

    // Tests for save method
    @Test
    void save_WhenValidCredentialDto_ShouldReturnSavedCredentialDto() {
        // Given
        when(credentialRepository.save(any(Credential.class))).thenReturn(testCredential);

        // When
        CredentialDto result = credentialService.save(testCredentialDto);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(RoleBasedAuthority.ROLE_USER, result.getRoleBasedAuthority());
        assertTrue(result.getIsEnabled());
        verify(credentialRepository, times(1)).save(any(Credential.class));
    }

    @Test
    void save_WhenRepositoryFails_ShouldPropagateException() {
        // Given
        when(credentialRepository.save(any(Credential.class))).thenThrow(new RuntimeException("Database constraint violation"));

        // When & Then
        assertThrows(RuntimeException.class, () -> credentialService.save(testCredentialDto));
        verify(credentialRepository, times(1)).save(any(Credential.class));
    }

    @Test
    void save_WhenCredentialWithDisabledAccount_ShouldSaveCorrectly() {
        // Given
        testCredentialDto.setIsEnabled(false);
        testCredentialDto.setIsAccountNonLocked(false);
        
        Credential disabledCredential = Credential.builder()
                .credentialId(1)
                .username("testuser")
                .isEnabled(false)
                .isAccountNonLocked(false)
                .user(testUser)
                .build();
        
        when(credentialRepository.save(any(Credential.class))).thenReturn(disabledCredential);

        // When
        CredentialDto result = credentialService.save(testCredentialDto);

        // Then
        assertNotNull(result);
        assertFalse(result.getIsEnabled());
        assertFalse(result.getIsAccountNonLocked());
        verify(credentialRepository, times(1)).save(any(Credential.class));
    }

    // Tests for update method (single parameter)
    @Test
    void update_WhenValidCredentialDto_ShouldReturnUpdatedCredentialDto() {
        // Given
        testCredentialDto.setUsername("updateduser");
        Credential updatedCredential = Credential.builder()
                .credentialId(1)
                .username("updateduser")
                .password("$2a$10$hashedpassword")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .user(testUser)
                .build();
        
        when(credentialRepository.save(any(Credential.class))).thenReturn(updatedCredential);

        // When
        CredentialDto result = credentialService.update(testCredentialDto);

        // Then
        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        verify(credentialRepository, times(1)).save(any(Credential.class));
    }

    @Test
    void update_WhenRepositoryFails_ShouldPropagateException() {
        // Given
        when(credentialRepository.save(any(Credential.class))).thenThrow(new RuntimeException("Update failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> credentialService.update(testCredentialDto));
        verify(credentialRepository, times(1)).save(any(Credential.class));
    }

    // Tests for update method (with credentialId parameter)
    @Test
    void update_WhenValidCredentialIdAndDto_ShouldReturnUpdatedCredentialDto() {
        // Given
        Integer credentialId = 1;
        when(credentialRepository.findById(credentialId)).thenReturn(Optional.of(testCredential));
        when(credentialRepository.save(any(Credential.class))).thenReturn(testCredential);

        // When
        CredentialDto result = credentialService.update(credentialId, testCredentialDto);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(credentialRepository, times(1)).findById(credentialId);
        verify(credentialRepository, times(1)).save(any(Credential.class));
    }

    @Test
    void update_WhenCredentialIdDoesNotExist_ShouldThrowCredentialNotFoundException() {
        // Given
        Integer credentialId = 999;
        when(credentialRepository.findById(credentialId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CredentialNotFoundException.class, 
                () -> credentialService.update(credentialId, testCredentialDto));
        verify(credentialRepository, times(1)).findById(credentialId);
        verify(credentialRepository, never()).save(any(Credential.class));
    }

    // Tests for deleteById method
    @Test
    void deleteById_WhenValidCredentialId_ShouldCallRepositoryDelete() {
        // Given
        Integer credentialId = 1;

        // When
        credentialService.deleteById(credentialId);

        // Then
        verify(credentialRepository, times(1)).deleteById(credentialId);
    }

    @Test
    void deleteById_WhenNullCredentialId_ShouldCallRepositoryWithNull() {
        // Given
        Integer credentialId = null;

        // When
        credentialService.deleteById(credentialId);

        // Then
        verify(credentialRepository, times(1)).deleteById(credentialId);
    }

    @Test
    void deleteById_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        Integer credentialId = 1;
        doThrow(new RuntimeException("Foreign key constraint")).when(credentialRepository).deleteById(credentialId);

        // When & Then
        assertThrows(RuntimeException.class, () -> credentialService.deleteById(credentialId));
        verify(credentialRepository, times(1)).deleteById(credentialId);
    }

    // Tests for findByUsername method
    @Test
    void findByUsername_WhenCredentialExists_ShouldReturnCredentialDto() {
        // Given
        String username = "testuser";
        when(credentialRepository.findByUsername(username)).thenReturn(Optional.of(testCredential));

        // When
        CredentialDto result = credentialService.findByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(RoleBasedAuthority.ROLE_USER, result.getRoleBasedAuthority());
        verify(credentialRepository, times(1)).findByUsername(username);
    }

    @Test
    void findByUsername_WhenCredentialDoesNotExist_ShouldThrowUserObjectNotFoundException() {
        // Given
        String username = "nonexistent";
        when(credentialRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        UserObjectNotFoundException exception = assertThrows(
                UserObjectNotFoundException.class,
                () -> credentialService.findByUsername(username)
        );
        
        assertEquals("#### Credential with username: nonexistent not found! ####", exception.getMessage());
        verify(credentialRepository, times(1)).findByUsername(username);
    }

    @Test
    void findByUsername_WhenEmptyUsername_ShouldCallRepositoryWithEmptyString() {
        // Given
        String username = "";
        when(credentialRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserObjectNotFoundException.class, () -> credentialService.findByUsername(username));
        verify(credentialRepository, times(1)).findByUsername(username);
    }
}