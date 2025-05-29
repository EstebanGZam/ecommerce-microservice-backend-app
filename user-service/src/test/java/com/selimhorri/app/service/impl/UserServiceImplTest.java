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
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDto testUserDto;
    private Credential testCredential;
    private CredentialDto testCredentialDto;

    @BeforeEach
    void setUp() {
        // Setup test credential
        testCredential = Credential.builder()
                .credentialId(1)
                .username("testuser")
                .password("hashedpassword")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        testCredentialDto = CredentialDto.builder()
                .credentialId(1)
                .username("testuser")
                .password("hashedpassword")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        // Setup test user
        testUser = User.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .imageUrl("http://example.com/image.jpg")
                .credential(testCredential)
                .build();

        testUserDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .imageUrl("http://example.com/image.jpg")
                .credentialDto(testCredentialDto)
                .build();

        // Set bidirectional relationship
        testCredential.setUser(testUser);
    }

    // Tests for findAll method
    @Test
    void findAll_WhenUsersExist_ShouldReturnListOfUserDtos() {
        // Given
        User secondUser = User.builder()
                .userId(2)
                .firstName("Jane")
                .lastName("Smith")
                .credential(Credential.builder()
                        .credentialId(2)
                        .username("janesmith")
                        .build())
                .build();
        secondUser.getCredential().setUser(secondUser);

        List<User> users = Arrays.asList(testUser, secondUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserDto> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findAll_WhenNoUsersExist_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<UserDto> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findAll_WhenDuplicateUsersExist_ShouldReturnDistinctUsers() {
        // Given
        List<User> usersWithDuplicates = Arrays.asList(testUser, testUser);
        when(userRepository.findAll()).thenReturn(usersWithDuplicates);

        // When
        List<UserDto> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findAll();
    }

    // Tests for findById method
    @Test
    void findById_WhenUserExists_ShouldReturnUserDto() {
        // Given
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.findById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("testuser", result.getCredentialDto().getUsername());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldThrowUserObjectNotFoundException() {
        // Given
        Integer userId = 999;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        UserObjectNotFoundException exception = assertThrows(
                UserObjectNotFoundException.class,
                () -> userService.findById(userId));

        assertEquals("User with id: 999 not found", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findById_WhenNullId_ShouldCallRepositoryWithNull() {
        // Given
        Integer userId = null;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserObjectNotFoundException.class, () -> userService.findById(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    // Tests for save method
    @Test
    void save_WhenValidUserDto_ShouldReturnSavedUserDto() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.save(testUserDto);

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("testuser", result.getCredentialDto().getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void save_WhenRepositoryFails_ShouldPropagateException() {
        // Given
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.save(testUserDto));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void save_WhenUserDtoWithNullFields_ShouldHandleGracefully() {
        // Given
        UserDto userWithNulls = UserDto.builder()
                .firstName(null)
                .lastName(null)
                .credentialDto(testCredentialDto)
                .build();
        User savedUser = User.builder()
                .userId(1)
                .firstName(null)
                .lastName(null)
                .credential(testCredential)
                .build();
        savedUser.getCredential().setUser(savedUser);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserDto result = userService.save(userWithNulls);

        // Then
        assertNotNull(result);
        assertNull(result.getFirstName());
        assertNull(result.getLastName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    // Tests for update method (single parameter)
    @Test
    void update_WhenValidUserDto_ShouldReturnUpdatedUserDto() {
        // Given
        testUserDto.setFirstName("UpdatedJohn");
        User updatedUser = User.builder()
                .userId(1)
                .firstName("UpdatedJohn")
                .lastName("Doe")
                .credential(testCredential)
                .build();
        updatedUser.getCredential().setUser(updatedUser);

        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        UserDto result = userService.update(testUserDto);

        // Then
        assertNotNull(result);
        assertEquals("UpdatedJohn", result.getFirstName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_WhenRepositoryFails_ShouldPropagateException() {
        // Given
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Update failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.update(testUserDto));
        verify(userRepository, times(1)).save(any(User.class));
    }

    // Tests for update method (with userId parameter)
    @Test
    void update_WhenValidUserIdAndDto_ShouldReturnUpdatedUserDto() {
        // Given
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.update(userId, testUserDto);

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_WhenUserIdDoesNotExist_ShouldThrowUserObjectNotFoundException() {
        // Given
        Integer userId = 999;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserObjectNotFoundException.class,
                () -> userService.update(userId, testUserDto));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    // Tests for deleteById method
    @Test
    void deleteById_WhenValidUserId_ShouldCallRepositoryDelete() {
        // Given
        Integer userId = 1;

        // When
        userService.deleteById(userId);

        // Then
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteById_WhenNullUserId_ShouldCallRepositoryWithNull() {
        // Given
        Integer userId = null;

        // When
        userService.deleteById(userId);

        // Then
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteById_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        Integer userId = 1;
        doThrow(new RuntimeException("Delete failed")).when(userRepository).deleteById(userId);

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.deleteById(userId));
        verify(userRepository, times(1)).deleteById(userId);
    }

    // Tests for findByUsername method
    @Test
    void findByUsername_WhenUserExists_ShouldReturnUserDto() {
        // Given
        String username = "testuser";
        when(userRepository.findByCredentialUsername(username)).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.findByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("testuser", result.getCredentialDto().getUsername());
        verify(userRepository, times(1)).findByCredentialUsername(username);
    }

    @Test
    void findByUsername_WhenUserDoesNotExist_ShouldThrowUserObjectNotFoundException() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByCredentialUsername(username)).thenReturn(Optional.empty());

        // When & Then
        UserObjectNotFoundException exception = assertThrows(
                UserObjectNotFoundException.class,
                () -> userService.findByUsername(username));

        assertEquals("User with username: nonexistent not found", exception.getMessage());
        verify(userRepository, times(1)).findByCredentialUsername(username);
    }

    @Test
    void findByUsername_WhenNullUsername_ShouldCallRepositoryWithNull() {
        // Given
        String username = null;
        when(userRepository.findByCredentialUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserObjectNotFoundException.class, () -> userService.findByUsername(username));
        verify(userRepository, times(1)).findByCredentialUsername(username);
    }
}