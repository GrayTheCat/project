package com.epam.finaltask.service;

import com.epam.finaltask.model.PasswordResetToken;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.PasswordResetTokenRepository;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.impl.PasswordResetServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private JavaMailSender mailSender;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private PasswordResetServiceImpl passwordResetService;

    @Test
    void createAndSendResetToken_UserNotFound_DoesNothing() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        passwordResetService.createAndSendResetToken("test@test.com", "http://localhost");
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void createAndSendResetToken_UserExists_SendsEmail() {
        User user = new User();
        user.setEmail("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        passwordResetService.createAndSendResetToken("test@test.com", "http://localhost");
        verify(tokenRepository).deleteByUser_Id(any());
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void validateTokenAndResetPassword_TokenInvalid_ReturnsFalse() {
        when(tokenRepository.findByToken("invalid")).thenReturn(Optional.empty());
        boolean result = passwordResetService.validateTokenAndResetPassword("invalid", "newPass");
        assertFalse(result);
    }

    @Test
    void validateTokenAndResetPassword_TokenExpired_ReturnsFalse() {
        PasswordResetToken token = mock(PasswordResetToken.class);
        when(token.isExpired()).thenReturn(true);
        when(tokenRepository.findByToken("expired")).thenReturn(Optional.of(token));
        boolean result = passwordResetService.validateTokenAndResetPassword("expired", "newPass");
        assertFalse(result);
    }

    @Test
    void validateTokenAndResetPassword_ValidToken_ReturnsTrue() {
        User user = new User();
        PasswordResetToken token = new PasswordResetToken("valid", user);
        when(tokenRepository.findByToken("valid")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedPass");
        boolean result = passwordResetService.validateTokenAndResetPassword("valid", "newPass");
        assertTrue(result);
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
        assertEquals("encodedPass", user.getPassword());
    }
}
