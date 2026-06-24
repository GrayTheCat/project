package com.epam.finaltask.controller;

import com.epam.finaltask.config.SecurityConfig;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.UserAlreadyExistsException;
import com.epam.finaltask.model.RefreshToken;
import com.epam.finaltask.service.*;
import com.epam.finaltask.token.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private UserService userService;
    @MockBean private AuthenticationManager authManager;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private PasswordResetService passwordResetService;
    @MockBean private RefreshTokenService refreshTokenService;
    @MockBean
    private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;

    @Test
    void webRegister_Success() throws Exception {
        mockMvc.perform(post("/auth/web-register").with(csrf())
                        .param("username", "newUser")
                        .param("password", "Pass123")
                        .param("email", "new@mail.com")
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/sign-in?registered"));
        verify(userService, times(1)).register(any(UserDTO.class));
    }

    @Test
    void webRegister_UserExists() throws Exception {
        doThrow(new UserAlreadyExistsException("exists")).when(userService).register(any(UserDTO.class));
        mockMvc.perform(post("/auth/web-register").with(csrf())
                        .param("username", "existsUser")
                        .param("password", "Pass123")
                        .param("email", "exists@mail.com")
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/sign-up"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void webRegister_InvalidData() throws Exception {
        mockMvc.perform(post("/auth/web-register").with(csrf())
                        .param("username", "")
                        .param("password", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/sign-up"));
        verify(userService, never()).register(any());
    }

    @Test
    void forgotPassword_Success() throws Exception {
        mockMvc.perform(post("/auth/forgot-password").with(csrf())
                        .param("email", "test@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/forgot-password?success"));
        verify(passwordResetService, times(1)).createAndSendResetToken(eq("test@test.com"), anyString());
    }

    @Test
    void webLogin_Success() throws Exception {
        UserDTO user = new UserDTO();
        user.setActive(true);
        when(userService.getUserByUsername("user")).thenReturn(user);
        UserDetails mockUser = org.springframework.security.core.userdetails.User
                .withUsername("user")
                .password("encodedPassword")
                .authorities("USER")
                .build();
        when(userDetailsService.loadUserByUsername("user")).thenReturn(mockUser);
        doReturn(null).when(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        when(jwtService.generateToken(any())).thenReturn("mock-jwt-token");
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("mock-refresh-token");
        when(refreshTokenService.createRefreshToken("user")).thenReturn(mockRefreshToken);
        mockMvc.perform(post("/auth/web-login").with(csrf())
                        .param("username", "user")
                        .param("password", "Pass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(cookie().exists("JWT"))
                .andExpect(cookie().value("JWT", "mock-jwt-token"));
    }

    @Test
    void webLogin_BlockedUser() throws Exception {
        UserDTO user = new UserDTO();
        user.setActive(false);
        when(userService.getUserByUsername("blocked")).thenReturn(user);
        mockMvc.perform(post("/auth/web-login").with(csrf())
                        .param("username", "blocked")
                        .param("password", "Pass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/sign-in?error=user.blocked"));
    }

    @Test
    void webLogin_Error() throws Exception {
        when(userService.getUserByUsername("user")).thenThrow(new RuntimeException());
        mockMvc.perform(post("/auth/web-login").with(csrf())
                        .param("username", "user")
                        .param("password", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/sign-in?error=login.error"));
    }

    @Test
    void resetPassword_Success() throws Exception {
        when(passwordResetService.validateTokenAndResetPassword(anyString(), anyString())).thenReturn(true);
        mockMvc.perform(post("/auth/reset-password").with(csrf())
                        .param("token", "valid-token")
                        .param("password", "NewPass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/sign-in?resetSuccess"));
    }

    @Test
    void resetPassword_InvalidToken() throws Exception {
        when(passwordResetService.validateTokenAndResetPassword(anyString(), anyString())).thenReturn(false);
        mockMvc.perform(post("/auth/reset-password").with(csrf())
                        .param("token", "bad-token")
                        .param("password", "NewPass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/reset-password?token=bad-token&error"));
    }

    @Test
    void refreshToken_Success() throws Exception {
        RefreshToken token = new RefreshToken();
        token.setToken("refresh-token");
        token.setUser(new com.epam.finaltask.model.User());
        when(refreshTokenService.findByToken("refresh-token")).thenReturn(java.util.Optional.of(token));
        when(refreshTokenService.verifyExpiration(any())).thenReturn(token);
        mockMvc.perform(get("/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("REFRESH_TOKEN", "refresh-token")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "testUser")
    void logout_Success() throws Exception {
        mockMvc.perform(get("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(cookie().maxAge("JWT", 0))
                .andExpect(cookie().maxAge("REFRESH_TOKEN", 0));
        verify(refreshTokenService, times(1)).deleteByUsername("testUser");
    }

    @Test
    void showForgotPasswordForm_ReturnsView() throws Exception {
        mockMvc.perform(get("/auth/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/forgot-password"));
    }

    @Test
    void showResetPasswordForm_ReturnsView() throws Exception {
        mockMvc.perform(get("/auth/reset-password").param("token", "xyz"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-password"))
                .andExpect(model().attributeExists("resetDTO"));
    }

    @Test
    void refreshToken_InvalidToken_RedirectsToSignIn() throws Exception {
        when(refreshTokenService.findByToken(anyString())).thenReturn(java.util.Optional.empty());
        mockMvc.perform(get("/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("REFRESH_TOKEN", "invalid")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/sign-in?error=login.error"));
    }

    @Test
    void refreshToken_NoCookie_RedirectsToSignIn() throws Exception {
        mockMvc.perform(get("/auth/refresh"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/sign-in"));
    }

    @Test
    void processResetPassword_ValidationError() throws Exception {
        mockMvc.perform(post("/auth/reset-password").with(csrf())
                        .param("token", "")
                        .param("password", "short"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-password"));
    }

    @Test
    void refreshToken_ServiceError() throws Exception {
        when(refreshTokenService.findByToken(anyString()))
                .thenThrow(new RuntimeException("DB Error"));
        mockMvc.perform(get("/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("REFRESH_TOKEN", "any-token")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/sign-in?error=login.error"));
    }

    @Test
    void logout_NoPrincipal() throws Exception {
        mockMvc.perform(get("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verify(refreshTokenService, never()).deleteByUsername(anyString());
    }

    @Test
    void processResetPassword_InvalidToken_RedirectsWithError() throws Exception {
        when(passwordResetService.validateTokenAndResetPassword(anyString(), anyString())).thenReturn(false);
        mockMvc.perform(post("/auth/reset-password").with(csrf())
                        .param("token", "token")
                        .param("password", "Pass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/reset-password?token=token&error"));
    }

    @Test
    void refreshToken_UserNotFound_RedirectsToError() throws Exception {
        RefreshToken token = new RefreshToken();
        token.setToken("token");
        com.epam.finaltask.model.User user = new com.epam.finaltask.model.User();
        user.setUsername("ghostUser");
        token.setUser(user);
        when(refreshTokenService.findByToken("token")).thenReturn(java.util.Optional.of(token));
        when(refreshTokenService.verifyExpiration(any())).thenReturn(token);
        when(userDetailsService.loadUserByUsername("ghostUser"))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("not found"));
        mockMvc.perform(get("/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("REFRESH_TOKEN", "token")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/sign-in?error=login.error"));
    }

    @Test
    void logout_NoPrincipal_RedirectsHome() throws Exception {
        mockMvc.perform(get("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verify(refreshTokenService, never()).deleteByUsername(anyString());
    }

    @Test
    void processResetPassword_ValidationErrors() throws Exception {
        mockMvc.perform(post("/auth/reset-password").with(csrf())
                        .param("token", "")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/reset-password"));
    }

    @Test
    void logout_WhenNotAuthenticated_DoesNothingToToken() throws Exception {
        mockMvc.perform(get("/auth/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verify(refreshTokenService, never()).deleteByUsername(anyString());
    }

    @Test
    void refreshToken_SuccessWithRedirectUrl() throws Exception {
        RefreshToken token = new RefreshToken();
        token.setToken("refresh-token");
        token.setUser(new com.epam.finaltask.model.User());
        when(refreshTokenService.findByToken("refresh-token")).thenReturn(java.util.Optional.of(token));
        when(refreshTokenService.verifyExpiration(any())).thenReturn(token);
        mockMvc.perform(get("/auth/refresh")
                        .param("redirectUrl", "/admin")
                        .cookie(new jakarta.servlet.http.Cookie("REFRESH_TOKEN", "refresh-token")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void signIn_ReturnsView() throws Exception {
        mockMvc.perform(get("/auth/sign-in"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/sign-in"));
    }

    @Test
    void signUp_ReturnsView() throws Exception {
        mockMvc.perform(get("/auth/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/sign-up"))
                .andExpect(model().attributeExists("userDTO"));
    }

    @Test
    void refreshToken_WithMultipleCookies_FindsToken() throws Exception {
        RefreshToken token = new RefreshToken();
        token.setToken("valid-token");
        com.epam.finaltask.model.User user = new com.epam.finaltask.model.User();
        user.setUsername("testUser");
        token.setUser(user);
        when(refreshTokenService.findByToken("valid-token")).thenReturn(java.util.Optional.of(token));
        when(refreshTokenService.verifyExpiration(any())).thenReturn(token);
        when(userDetailsService.loadUserByUsername("testUser")).thenReturn(
                org.springframework.security.core.userdetails.User.withUsername("testUser").password("pw").authorities("USER").build());
        when(jwtService.generateToken(any())).thenReturn("new-jwt");
        mockMvc.perform(get("/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("OTHER_COOKIE", "someValue"),
                                new jakarta.servlet.http.Cookie("REFRESH_TOKEN", "valid-token"))) // <--- REFRESH_TOKEN другий
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }
}
