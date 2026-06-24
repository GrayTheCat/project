package com.epam.finaltask.controller;

import com.epam.finaltask.config.SecurityConfig;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private UserService userService;
    @MockBean private VoucherService voucherService;
    @MockBean private com.epam.finaltask.token.JwtService jwtService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean private com.epam.finaltask.service.RefreshTokenService refreshTokenService;
    @MockBean private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;

    @Test
    @WithMockUser(username = "testUser")
    void profile_ReturnsProfileView() throws Exception {
        UserDTO user = new UserDTO();
        user.setId("1");
        when(userService.getUserByUsername("testUser")).thenReturn(user);
        when(voucherService.findAllByUserId("1")).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile"))
                .andExpect(model().attributeExists("user", "myVouchers"));
    }

    @Test
    void profile_RedirectsWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/profile")
                        .with(anonymous()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testUser")
    void topUpBalance_Success() throws Exception {
        UserDTO user = new UserDTO();
        user.setBalance(BigDecimal.TEN);
        when(userService.getUserByUsername("testUser")).thenReturn(user);
        mockMvc.perform(post("/web/profile/top-up").with(csrf())
                        .param("amount", "50.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
        verify(userService).updateUser(eq("testUser"), any(UserDTO.class));
    }

    @Test
    @WithMockUser(username = "testUser")
    void topUpBalance_ValidAmount_UpdatesUser() throws Exception {
        UserDTO user = new UserDTO();
        user.setBalance(BigDecimal.TEN);
        when(userService.getUserByUsername("testUser")).thenReturn(user);
        mockMvc.perform(post("/web/profile/top-up").with(csrf())
                        .param("amount", "50.00"))
                .andExpect(status().is3xxRedirection());
        verify(userService).updateUser(eq("testUser"), any(UserDTO.class));
    }

    @Test
    void topUpBalance_NotAuthenticated_DoesNothing() throws Exception {
        mockMvc.perform(post("/web/profile/top-up").with(csrf())
                        .param("amount", "50.00"))
                .andExpect(status().isForbidden());
        verify(userService, never()).updateUser(anyString(), any());
    }

    @Test
    @WithMockUser(username = "testUser")
    void topUpBalance_InvalidAmount_DoesNothing() throws Exception {
        mockMvc.perform(post("/web/profile/top-up").with(csrf())
                        .param("amount", "-10.00"))
                .andExpect(status().is3xxRedirection());
        verify(userService, never()).updateUser(anyString(), any());
    }

    @Test
    @WithMockUser(username = "testUser")
    void topUpBalance_NullBalance_SetsZeroFirst() throws Exception {
        UserDTO user = new UserDTO();
        user.setBalance(null);
        when(userService.getUserByUsername("testUser")).thenReturn(user);
        mockMvc.perform(post("/web/profile/top-up").with(csrf())
                        .param("amount", "20.00"))
                .andExpect(status().is3xxRedirection());
        verify(userService).updateUser(anyString(), argThat(dto ->
                dto.getBalance().compareTo(new BigDecimal("20.00")) == 0
        ));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void toggleBlockUser_ChangesStatus() throws Exception {
        UserDTO user = new UserDTO();
        user.setUsername("someUser");
        user.setActive(true);
        when(userService.getUserByUsername("someUser")).thenReturn(user);
        mockMvc.perform(post("/web/users/someUser/block").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        verify(userService).changeAccountStatus(any(UserDTO.class));
    }
}