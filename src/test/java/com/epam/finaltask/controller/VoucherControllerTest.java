package com.epam.finaltask.controller;

import com.epam.finaltask.config.SecurityConfig;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VoucherController.class)
@Import(SecurityConfig.class)
class VoucherControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private VoucherService voucherService;
    @MockBean private UserService userService;
    @MockBean private com.epam.finaltask.token.JwtService jwtService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean private com.epam.finaltask.service.RefreshTokenService refreshTokenService;
    @MockBean private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;


    @Test
    @WithMockUser(username = "user")
    void orderVoucher_Success() throws Exception {
        UserDTO user = new UserDTO();
        user.setId("1");
        when(userService.getUserByUsername("user")).thenReturn(user);
        mockMvc.perform(post("/web/vouchers/1/order").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
        verify(voucherService).order("1", "1");
    }

    @Test
    @WithMockUser(username = "admin")
    void createTour_Success() throws Exception {
        mockMvc.perform(post("/web/vouchers/create").with(csrf())
                        .flashAttr("voucherDTO", new VoucherDTO()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
        verify(voucherService).create(any(VoucherDTO.class));
    }

    @Test
    @WithMockUser(username = "admin")
    void updateTour_Success() throws Exception {
        String validUuid = java.util.UUID.randomUUID().toString();
        mockMvc.perform(post("/web/vouchers/" + validUuid + "/update")
                        .with(csrf())
                        .param("id", validUuid)
                        .flashAttr("voucherDTO", new VoucherDTO()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    @WithMockUser(username = "manager")
    void changeVoucherStatus_RedirectsToManager() throws Exception {
        mockMvc.perform(post("/web/vouchers/1/status")
                        .with(csrf())
                        .header("Referer", "http://localhost/manager")
                        .param("status", "CONFIRMED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager#orders"));
    }

    @Test
    @WithMockUser(username = "admin")
    void toggleHotStatus_RedirectsToAdmin() throws Exception {
        mockMvc.perform(post("/web/vouchers/1/hot")
                        .with(csrf())
                        .header("Referer", "http://localhost/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin#tours"));
    }

    @Test
    @WithMockUser
    void toggleHotStatus_DefaultRedirect() throws Exception {
        mockMvc.perform(post("/web/vouchers/1/hot").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser(username = "admin")
    void changeVoucherStatus_RedirectsToAdmin() throws Exception {
        mockMvc.perform(post("/web/vouchers/1/status")
                        .with(csrf())
                        .header("Referer", "http://localhost/admin/dashboard")
                        .param("status", "CANCELLED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin#orders"));
    }

    @Test
    @WithMockUser(username = "user")
    void changeVoucherStatus_RedirectsToReferer() throws Exception {
        String customReferer = "http://localhost/some-other-page";
        mockMvc.perform(post("/web/vouchers/1/status")
                        .with(csrf())
                        .header("Referer", customReferer)
                        .param("status", "CONFIRMED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(customReferer));
    }

    @Test
    @WithMockUser(username = "user")
    void changeVoucherStatus_NoReferer_RedirectsHome() throws Exception {
        mockMvc.perform(post("/web/vouchers/1/status")
                        .with(csrf())
                        .param("status", "CONFIRMED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser(username = "manager")
    void toggleHotStatus_RedirectsToManagerTours() throws Exception {
        mockMvc.perform(post("/web/vouchers/1/hot")
                        .with(csrf())
                        .header("Referer", "http://localhost/manager/tours-list"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager#tours"));
        verify(voucherService).toggleHotStatus("1");
    }

    @Test
    @WithMockUser(username = "admin")
    void toggleHotStatus_RedirectsToAdminTours() throws Exception {
        mockMvc.perform(post("/web/vouchers/1/hot")
                        .with(csrf())
                        .header("Referer", "http://localhost/admin/tours-management"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin#tours"));
    }

    @Test
    @WithMockUser(username = "user")
    void toggleHotStatus_RedirectsToReferer() throws Exception {
        String customReferer = "http://localhost/other-page";
        mockMvc.perform(post("/web/vouchers/1/hot")
                        .with(csrf())
                        .header("Referer", customReferer))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(customReferer));
    }

    @Test
    @WithMockUser(username = "user")
    void toggleHotStatus_NoReferer_RedirectsHome() throws Exception {
        mockMvc.perform(post("/web/vouchers/1/hot")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTour_ShouldRedirectToAdmin_WhenTourExists() throws Exception {
        String tourId = "550e8400-e29b-41d4-a716-446655440000";
        mockMvc.perform(post("/web/vouchers/delete/{id}", tourId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
        verify(voucherService).delete(tourId);
    }
}