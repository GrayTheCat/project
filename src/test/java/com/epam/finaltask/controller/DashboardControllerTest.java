package com.epam.finaltask.controller;

import com.epam.finaltask.config.SecurityConfig;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private VoucherService voucherService;
    @MockBean private UserService userService;
    @MockBean private com.epam.finaltask.token.JwtService jwtService;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean private com.epam.finaltask.service.RefreshTokenService refreshTokenService;
    @MockBean private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;

    @Test
    void index_ShouldReturnIndexPage() throws Exception {
        Page<VoucherDTO> emptyPage = new PageImpl<>(Collections.emptyList());
        when(voucherService.findFiltered(any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString()))
                .thenReturn(emptyPage);
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("vouchers", "currentPage", "totalPages"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_ShouldReturnAdminPage_WhenAuthenticated() throws Exception {
        when(voucherService.findAll(anyInt(), anyInt(), anyString()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(userService.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin"));
    }

    @Test
    void admin_ShouldRedirectToSignIn_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/sign-in?error=error.unauthorized"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void manager_ShouldReturnManagerPage() throws Exception {
        when(voucherService.findAll(anyInt(), anyInt(), anyString()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/manager"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/manager"));
    }

    @GetMapping("/manager")
    public String managerPanel(Model model, Principal principal) {
        if (principal == null) return "redirect:/auth/sign-in";
        model.addAttribute("allVouchers", voucherService.findAll(0, 100, "id").getContent());
        model.addAttribute("orderedVouchers", voucherService.findAllOrdered());
        return "manager/manager";
    }
}