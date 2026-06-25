package com.epam.finaltask.service;

import com.epam.finaltask.exception.BusinessLogicException;
import com.epam.finaltask.model.RefreshToken;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.RefreshTokenRepository;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private RefreshTokenServiceImpl refreshTokenService;

    @Test
    void createRefreshToken_Success() {
        User user = new User();
        when(userRepository.findUserByUsername("user")).thenReturn(Optional.of(user));
        refreshTokenService.createRefreshToken("user");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void findByToken_DelegatesToRepository() {
        refreshTokenService.findByToken("token");
        verify(refreshTokenRepository).findByToken("token");
    }

    @Test
    void verifyExpiration_ValidToken_ReturnsToken() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().plusSeconds(3600));
        RefreshToken result = refreshTokenService.verifyExpiration(token);
        assertEquals(token, result);
    }

    @Test
    void verifyExpiration_ExpiredToken_ThrowsException() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().minusSeconds(3600));
        assertThrows(BusinessLogicException.class, () -> refreshTokenService.verifyExpiration(token));
        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void deleteByUsername_UserExists_DeletesToken() {
        User user = new User();
        when(userRepository.findUserByUsername("user")).thenReturn(Optional.of(user));
        refreshTokenService.deleteByUsername("user");
        verify(refreshTokenRepository).deleteByUser(user);
    }
}
