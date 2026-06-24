package com.epam.finaltask.controller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@Slf4j
@Controller
@RequestMapping("/web/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;
    private final UserService userService;

    @PostMapping("/{id}/order")
    public String orderVoucher(@PathVariable String id, Principal principal) {
        if (principal == null) return "redirect:/auth/sign-in";
        log.info("User '{}' is attempting to order voucher '{}'", principal.getName(), id);
        UserDTO user = userService.getUserByUsername(principal.getName());
        voucherService.order(id, user.getId());
        log.info("Voucher '{}' successfully ordered by user '{}'", id, principal.getName());
        return "redirect:/profile";
    }

    @PostMapping("/create")
    public String createTour(@ModelAttribute VoucherDTO voucherDTO, Principal principal) {
        voucherDTO.setStatus("REGISTERED");
        voucherService.create(voucherDTO);
        log.info("Admin '{}' created a new tour: '{}'", principal.getName(), voucherDTO.getTitle());
        return "redirect:/admin";
    }

    @PostMapping("/{id}/update")
    public String updateTour(@PathVariable String id, @ModelAttribute VoucherDTO voucherDTO, Principal principal) {
        voucherService.update(id, voucherDTO);
        log.info("Admin '{}' updated tour with ID: '{}'", principal.getName(), id);
        return "redirect:/admin";
    }

    @PostMapping("/{id}/status")
    public String changeVoucherStatus(@PathVariable String id,
                                      @RequestParam String status,
                                      @RequestParam(required = false) String reason,
                                      HttpServletRequest request,
                                      Principal principal) {

        voucherService.changeStatus(id, status, reason);
        log.info("User '{}' changed status of voucher '{}' to '{}'. Reason: {}", principal.getName(), id, status, reason);
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/manager")) {
            return "redirect:/manager#orders";
        } else if (referer != null && referer.contains("/admin")) {
            return "redirect:/admin#orders";
        }
        return "redirect:" + (referer != null ? referer : "/");
    }

    @PostMapping("/{id}/hot")
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
}
