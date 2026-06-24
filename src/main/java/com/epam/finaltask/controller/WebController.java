package com.epam.finaltask.controller;

import com.epam.finaltask.dto.ResetPasswordDTO;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.UserAlreadyExistsException;
import com.epam.finaltask.model.RefreshToken;
import com.epam.finaltask.service.PasswordResetService;
import com.epam.finaltask.service.RefreshTokenService;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import com.epam.finaltask.token.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebController {

    private final VoucherService voucherService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenService refreshTokenService;

    @GetMapping("/")
    public String index(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String tourType,
            @RequestParam(required = false) String transferType,
            @RequestParam(required = false) String hotelType,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        int size = 6;

        Page<VoucherDTO> voucherPage = voucherService.findFiltered(
                search, maxPrice, tourType, transferType, hotelType, page, size, "id"
        );

        model.addAttribute("vouchers", voucherPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", voucherPage.getTotalPages());

        return "index";
    }

    @GetMapping("/auth/sign-in")
    public String signIn() { return "auth/sign-in"; }

    @GetMapping("/auth/sign-up")
    public String signUp(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "auth/sign-up";
    }

    @PostMapping("/auth/web-login")
    public String webLogin(@RequestParam String username, @RequestParam String password, HttpServletResponse response) {
        try {
            UserDTO userDTO = userService.getUserByUsername(username);
            if (!userDTO.isActive()) return "redirect:/auth/sign-in?error=user.blocked";
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String token = jwtService.generateToken(userDetails);
            Cookie jwtCookie = new Cookie("JWT", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(15 * 60);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(username);
            Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken.getToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(jwtCookie);
            response.addCookie(refreshCookie);
            return "redirect:/profile";
        } catch (Exception e) {
            return "redirect:/auth/sign-in?error=login.error";
        }
    }

    @PostMapping("/auth/web-register")
    public String webRegister(@Valid @ModelAttribute("userDTO") UserDTO userDTO,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            log.warn("User registration validation failed for username '{}'. Errors: {}", userDTO.getUsername(), bindingResult.getErrorCount());
            return "auth/sign-up";
        }
        try {
            userService.register(userDTO);
            log.info("Successfully registered new user: {}", userDTO.getUsername());
            return "redirect:/auth/sign-in?registered";
        } catch (UserAlreadyExistsException e) {
            log.warn("Registration failed. User '{}' already exists.", userDTO.getUsername());
            model.addAttribute("errorMessage", "user.already.exists");
            return "auth/sign-up";
        }
    }

    @GetMapping("/auth/logout")
    public String logout(HttpServletResponse response, Principal principal) {
        if (principal != null) {
            refreshTokenService.deleteByUsername(principal.getName());
        }
        Cookie jwtCookie = new Cookie("JWT", null);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", null);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        if (principal == null) return "redirect:/auth/sign-in";
        UserDTO user = userService.getUserByUsername(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("myVouchers", voucherService.findAllByUserId(user.getId()));
        return "user/profile";
    }

    @PostMapping("/web/vouchers/{id}/order")
    public String orderVoucher(@PathVariable String id, Principal principal) {
        if (principal == null) return "redirect:/auth/sign-in";
        log.info("User '{}' is attempting to order voucher '{}'", principal.getName(), id);
        UserDTO user = userService.getUserByUsername(principal.getName());
        voucherService.order(id, user.getId());
        log.info("Voucher '{}' successfully ordered by user '{}'", id, principal.getName());
        return "redirect:/profile";
    }

    @GetMapping("/admin")
    public String admin(Model model, Principal principal) {
        if (principal == null) return "redirect:/auth/sign-in";
        model.addAttribute("vouchers", voucherService.findAll(0, 100, "id").getContent());
        model.addAttribute("users", userService.findAll());
        return "admin/admin";
    }

    @PostMapping("/web/users/{username}/block")
    @ResponseBody
    public Map<String, Boolean> toggleBlockUser(@PathVariable String username, Principal principal) {
        UserDTO userDTO = userService.getUserByUsername(username);
        boolean newStatus = !userDTO.isActive();
        userDTO.setActive(!userDTO.isActive());
        userService.changeAccountStatus(userDTO);
        log.info("Admin '{}' changed active status of user '{}' to {}", principal.getName(), username, newStatus);
        return Collections.singletonMap("active", userDTO.isActive());
    }

    @PostMapping("/web/vouchers/create")
    public String createTour(@ModelAttribute VoucherDTO voucherDTO, Principal principal) {
        voucherDTO.setStatus("REGISTERED");
        voucherService.create(voucherDTO);
        log.info("Admin '{}' created a new tour: '{}'", principal.getName(), voucherDTO.getTitle());
        return "redirect:/admin";
    }

    @PostMapping("/web/profile/top-up")
    public String topUpBalance(@RequestParam BigDecimal amount, Principal principal) {
        if (principal != null && amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            UserDTO userDTO = userService.getUserByUsername(principal.getName());
            if (userDTO.getBalance() == null) {
                userDTO.setBalance(BigDecimal.ZERO);
            }
            userDTO.setBalance(userDTO.getBalance().add(amount));
            userService.updateUser(principal.getName(), userDTO);
            log.info("User '{}' topped up balance by {}", principal.getName(), amount);
        }
        return "redirect:/profile";
    }

    @GetMapping("/manager")
    public String managerPanel(Model model) {
        model.addAttribute("allVouchers", voucherService.findAll(0, 100, "id").getContent());
        model.addAttribute("orderedVouchers", voucherService.findAllOrdered());
        return "manager/manager";
    }

    @PostMapping("/web/vouchers/{id}/update")
    public String updateTour(@PathVariable String id, @ModelAttribute VoucherDTO voucherDTO, Principal principal) {
        voucherService.update(id, voucherDTO);
        log.info("Admin '{}' updated tour with ID: '{}'", principal.getName(), id);
        return "redirect:/admin";
    }

    @PostMapping("/web/vouchers/{id}/status")
    public String changeVoucherStatus(@PathVariable String id,
                                      @RequestParam String status,
                                      @RequestParam(required = false) String reason,
                                      Principal principal) {
        voucherService.changeStatus(id, status, reason);
        log.info("Manager '{}' changed status of voucher '{}' to '{}'. Reason: {}", principal.getName(), id, status, reason);
        return "redirect:/manager";
    }

    @PostMapping("/web/vouchers/{id}/hot")
    public String toggleVoucherHotStatus(@PathVariable String id, HttpServletRequest request, Principal principal) {
        voucherService.toggleHotStatus(id);
        log.info("User '{}' toggled HOT status for voucher '{}'", principal.getName(), id);
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/manager")) {
            return "redirect:/manager#tours";
        } else if (referer != null && referer.contains("/admin")) {
            return "redirect:/admin#tours";
        }
        return "redirect:" + (referer != null ? referer : "/");
    }

    @GetMapping("/auth/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/auth/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, HttpServletRequest request) {
        String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        passwordResetService.createAndSendResetToken(email, appUrl);
        return "redirect:/auth/forgot-password?success";
    }

    @GetMapping("/auth/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setToken(token);
        model.addAttribute("resetDTO", dto);
        return "auth/reset-password";
    }

    @PostMapping("/auth/reset-password")
    public String processResetPassword(@Valid @ModelAttribute("resetDTO") ResetPasswordDTO resetDTO,
                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }
        boolean success = passwordResetService.validateTokenAndResetPassword(resetDTO.getToken(), resetDTO.getPassword());
        if (success) {
            return "redirect:/auth/sign-in?resetSuccess";
        } else {
            return "redirect:/auth/reset-password?token=" + resetDTO.getToken() + "&error";
        }
    }

    @GetMapping("/auth/refresh")
    public String refreshToken(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(required = false) String redirectUrl) {
        String refreshTokenString = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("REFRESH_TOKEN".equals(cookie.getName())) {
                    refreshTokenString = cookie.getValue();
                }
            }
        }
        if (refreshTokenString != null) {
            try {
                RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenString)
                        .map(refreshTokenService::verifyExpiration)
                        .orElseThrow(() -> new Exception("Invalid refresh token"));
                UserDetails userDetails = userDetailsService.loadUserByUsername(refreshToken.getUser().getUsername());
                String newJwt = jwtService.generateToken(userDetails);
                Cookie jwtCookie = new Cookie("JWT", newJwt);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(15 * 60);
                response.addCookie(jwtCookie);
                return "redirect:" + (redirectUrl != null ? redirectUrl : "/profile");
            } catch (Exception e) {
                return "redirect:/auth/sign-in?error=login.error";
            }
        }
        return "redirect:/auth/sign-in";
    }
}