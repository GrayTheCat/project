package com.epam.finaltask.controller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final VoucherService voucherService;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        //if (principal == null) return "redirect:/auth/sign-in";
        UserDTO user = userService.getUserByUsername(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("myVouchers", voucherService.findAllByUserId(user.getId()));
        return "user/profile";
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
}
