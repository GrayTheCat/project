package com.epam.finaltask.controller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import com.epam.finaltask.token.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequiredArgsConstructor
public class WebController {

    private final VoucherService voucherService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

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
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            UserDetails user = userDetailsService.loadUserByUsername(username);
            String token = jwtService.generateToken(user);

            Cookie cookie = new Cookie("JWT", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            return "redirect:/profile";
        } catch (Exception e) {
            return "redirect:/auth/sign-in?error";
        }
    }

    @PostMapping("/auth/web-register")
    public String webRegister(@Valid @ModelAttribute("userDTO") UserDTO userDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "auth/sign-up";
        }
        userService.register(userDTO);
        return "redirect:/auth/sign-in?registered";
    }

    @GetMapping("/auth/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
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
        UserDTO user = userService.getUserByUsername(principal.getName());
        voucherService.order(id, user.getId());
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
    public String toggleBlockUser(@PathVariable String username) {
        UserDTO userDTO = userService.getUserByUsername(username);
        userDTO.setActive(!userDTO.isActive());
        userService.changeAccountStatus(userDTO);
        return "redirect:/admin";
    }

    @PostMapping("/web/vouchers/create")
    public String createTour(@ModelAttribute VoucherDTO voucherDTO) {
        voucherDTO.setStatus("REGISTERED");
        voucherService.create(voucherDTO);
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
    public String updateTour(@PathVariable String id, @ModelAttribute VoucherDTO voucherDTO) {
        voucherService.update(id, voucherDTO);
        return "redirect:/admin";
    }

    @PostMapping("/web/vouchers/{id}/status")
    public String changeVoucherStatus(@PathVariable String id,
                                      @RequestParam String status,
                                      @RequestParam(required = false) String reason) {
        voucherService.changeStatus(id, status, reason);
        return "redirect:/manager";
    }

    @PostMapping("/web/vouchers/{id}/hot")
    public String toggleVoucherHotStatus(@PathVariable String id, HttpServletRequest request) {
        voucherService.toggleHotStatus(id);
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/manager")) {
            return "redirect:/manager#tours";
        } else if (referer != null && referer.contains("/admin")) {
            return "redirect:/admin#tours";
        }
        return "redirect:" + (referer != null ? referer : "/");
    }
}