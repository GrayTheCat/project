package com.epam.finaltask.controller;

import com.epam.finaltask.dto.ResetPasswordDTO;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.UserAlreadyExistsException;
import com.epam.finaltask.model.RefreshToken;
import com.epam.finaltask.service.PasswordResetService;
import com.epam.finaltask.service.RefreshTokenService;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.token.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenService refreshTokenService;

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
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isManager = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
            if (isAdmin) {
                return "redirect:/admin";
            } else if (isManager) {
                return "redirect:/manager";
            } else {
                return "redirect:/profile";
            }
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

    @RequestMapping("/access-denied")
    public void accessDenied() {
        throw new AccessDeniedException("Access Denied");
    }
}
