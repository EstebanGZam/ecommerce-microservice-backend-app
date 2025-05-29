package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
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
import com.selimhorri.app.domain.VerificationToken;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.VerificationTokenDto;
import com.selimhorri.app.exception.wrapper.VerificationTokenNotFoundException;
import com.selimhorri.app.repository.VerificationTokenRepository;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceImplTest {

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @InjectMocks
    private VerificationTokenServiceImpl verificationTokenService;

    private VerificationToken testVerificationToken;
    private VerificationTokenDto testVerificationTokenDto;
    private Credential testCredential;
    private CredentialDto testCredentialDto;

    @BeforeEach
    void setUp() {
        // Setup test credential
        testCredential = Credential.builder()
                .credentialId(1)
                .username("testuser")
                .password("$2a$10$hashedpassword")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(false) // Typically false when verification token is created
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        testCredentialDto = CredentialDto.builder()
                .credentialId(1)
                .username("testuser")
                .password("$2a$10$hashedpassword")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(false)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        // Setup test verification token
        testVerificationToken = VerificationToken.builder()
                .verificationTokenId(1)
                .token("abc123-def456-ghi789")
                .expireDate(LocalDate.now().plusDays(1))
                .credential(testCredential)
                .build();

        testVerificationTokenDto = VerificationTokenDto.builder()
                .verificationTokenId(1)
                .token("abc123-def456-ghi789")
                .expireDate(LocalDate.now().plusDays(1))
                .credentialDto(testCredentialDto)
                .build();
    }

    // Tests for findAll method
    @Test
    void findAll_WhenVerificationTokensExist_ShouldReturnListOfVerificationTokenDtos() {
        // Given
        VerificationToken secondToken = VerificationToken.builder()
                .verificationTokenId(2)
                .token("xyz789-uvw456-rst123")
                .expireDate(LocalDate.now().plusDays(2))
                .credential(testCredential)
                .build();
        
        List<VerificationToken> tokens = Arrays.asList(testVerificationToken, secondToken);
        when(verificationTokenRepository.findAll()).thenReturn(tokens);

        // When
        List<VerificationTokenDto> result = verificationTokenService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("abc123-def456-ghi789", result.get(0).getToken());
        assertEquals("xyz789-uvw456-rst123", result.get(1).getToken());
        verify(verificationTokenRepository, times(1)).findAll();
    }

    @Test
    void findAll_WhenNoVerificationTokensExist_ShouldReturnEmptyList() {
        // Given
        when(verificationTokenRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<VerificationTokenDto> result = verificationTokenService.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(verificationTokenRepository, times(1)).findAll();
    }

    @Test
    void findAll_WhenDuplicateTokensExist_ShouldReturnDistinctTokens() {
        // Given
        List<VerificationToken> tokensWithDuplicates = Arrays.asList(testVerificationToken, testVerificationToken);
        when(verificationTokenRepository.findAll()).thenReturn(tokensWithDuplicates);

        // When
        List<VerificationTokenDto> result = verificationTokenService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(verificationTokenRepository, times(1)).findAll();
    }

    // Tests for findById method
    @Test
    void findById_WhenVerificationTokenExists_ShouldReturnVerificationTokenDto() {
        // Given
        Integer tokenId = 1;
        when(verificationTokenRepository.findById(tokenId)).thenReturn(Optional.of(testVerificationToken));

        // When
        VerificationTokenDto result = verificationTokenService.findById(tokenId);

        // Then
        assertNotNull(result);
        assertEquals(tokenId, result.getVerificationTokenId());
        assertEquals("abc123-def456-ghi789", result.getToken());
        assertEquals(LocalDate.now().plusDays(1), result.getExpireDate());
        assertEquals("testuser", result.getCredentialDto().getUsername());
        verify(verificationTokenRepository, times(1)).findById(tokenId);
    }

    @Test
    void findById_WhenVerificationTokenDoesNotExist_ShouldThrowVerificationTokenNotFoundException() {
        // Given
        Integer tokenId = 999;
        when(verificationTokenRepository.findById(tokenId)).thenReturn(Optional.empty());

        // When & Then
        VerificationTokenNotFoundException exception = assertThrows(
                VerificationTokenNotFoundException.class,
                () -> verificationTokenService.findById(tokenId)
        );
        
        assertEquals("#### VerificationToken with id: 999 not found! ####", exception.getMessage());
        verify(verificationTokenRepository, times(1)).findById(tokenId);
    }

    @Test
    void findById_WhenNullId_ShouldCallRepositoryWithNull() {
        // Given
        Integer tokenId = null;
        when(verificationTokenRepository.findById(tokenId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(VerificationTokenNotFoundException.class, () -> verificationTokenService.findById(tokenId));
        verify(verificationTokenRepository, times(1)).findById(tokenId);
    }

    // Tests for save method
    @Test
    void save_WhenValidVerificationTokenDto_ShouldReturnSavedVerificationTokenDto() {
        // Given
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(testVerificationToken);

        // When
        VerificationTokenDto result = verificationTokenService.save(testVerificationTokenDto);

        // Then
        assertNotNull(result);
        assertEquals("abc123-def456-ghi789", result.getToken());
        assertEquals(LocalDate.now().plusDays(1), result.getExpireDate());
        assertEquals("testuser", result.getCredentialDto().getUsername());
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
    }

    @Test
    void save_WhenRepositoryFails_ShouldPropagateException() {
        // Given
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenThrow(new RuntimeException("Database constraint violation"));

        // When & Then
        assertThrows(RuntimeException.class, () -> verificationTokenService.save(testVerificationTokenDto));
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
    }

    @Test
    void save_WhenTokenWithExpiredDate_ShouldSaveCorrectly() {
        // Given
        LocalDate expiredDate = LocalDate.now().minusDays(1);
        testVerificationTokenDto.setExpireDate(expiredDate);
        
        VerificationToken expiredToken = VerificationToken.builder()
                .verificationTokenId(1)
                .token("expired-token-123")
                .expireDate(expiredDate)
                .credential(testCredential)
                .build();
        
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(expiredToken);

        // When
        VerificationTokenDto result = verificationTokenService.save(testVerificationTokenDto);

        // Then
        assertNotNull(result);
        assertEquals(expiredDate, result.getExpireDate());
        assertTrue(result.getExpireDate().isBefore(LocalDate.now()));
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
    }

    // Tests for update method (single parameter)
    @Test
    void update_WhenValidVerificationTokenDto_ShouldReturnUpdatedVerificationTokenDto() {
        // Given
        testVerificationTokenDto.setToken("updated-token-456");
        VerificationToken updatedToken = VerificationToken.builder()
                .verificationTokenId(1)
                .token("updated-token-456")
                .expireDate(LocalDate.now().plusDays(1))
                .credential(testCredential)
                .build();
        
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(updatedToken);

        // When
        VerificationTokenDto result = verificationTokenService.update(testVerificationTokenDto);

        // Then
        assertNotNull(result);
        assertEquals("updated-token-456", result.getToken());
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
    }

    @Test
    void update_WhenRepositoryFails_ShouldPropagateException() {
        // Given
        when(verificationTokenRepository.save(any(VerificationToken.class)))
                .thenThrow(new RuntimeException("Update failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> verificationTokenService.update(testVerificationTokenDto));
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
    }

    // Tests for update method (with tokenId parameter)
    @Test
    void update_WhenValidTokenIdAndDto_ShouldReturnUpdatedVerificationTokenDto() {
        // Given
        Integer tokenId = 1;
        when(verificationTokenRepository.findById(tokenId)).thenReturn(Optional.of(testVerificationToken));
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(testVerificationToken);

        // When
        VerificationTokenDto result = verificationTokenService.update(tokenId, testVerificationTokenDto);

        // Then
        assertNotNull(result);
        assertEquals("abc123-def456-ghi789", result.getToken());
        verify(verificationTokenRepository, times(1)).findById(tokenId);
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
    }

    @Test
    void update_WhenTokenIdDoesNotExist_ShouldThrowVerificationTokenNotFoundException() {
        // Given
        Integer tokenId = 999;
        when(verificationTokenRepository.findById(tokenId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(VerificationTokenNotFoundException.class, 
                () -> verificationTokenService.update(tokenId, testVerificationTokenDto));
        verify(verificationTokenRepository, times(1)).findById(tokenId);
        verify(verificationTokenRepository, never()).save(any(VerificationToken.class));
    }

    // Tests for deleteById method
    @Test
    void deleteById_WhenValidTokenId_ShouldCallRepositoryDelete() {
        // Given
        Integer tokenId = 1;

        // When
        verificationTokenService.deleteById(tokenId);

        // Then
        verify(verificationTokenRepository, times(1)).deleteById(tokenId);
    }

    @Test
    void deleteById_WhenNullTokenId_ShouldCallRepositoryWithNull() {
        // Given
        Integer tokenId = null;

        // When
        verificationTokenService.deleteById(tokenId);

        // Then
        verify(verificationTokenRepository, times(1)).deleteById(tokenId);
    }

    @Test
    void deleteById_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        Integer tokenId = 1;
        doThrow(new RuntimeException("Token cleanup failed")).when(verificationTokenRepository).deleteById(tokenId);

        // When & Then
        assertThrows(RuntimeException.class, () -> verificationTokenService.deleteById(tokenId));
        verify(verificationTokenRepository, times(1)).deleteById(tokenId);
    }
}