package com.epam.finaltask.controller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final VoucherService voucherService;
    private final UserService userService;

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

    @GetMapping("/admin")
    public String admin(Model model, Principal principal) {
        if (principal == null) return "redirect:/auth/sign-in";
        model.addAttribute("vouchers", voucherService.findAll(0, 100, "id").getContent());
        model.addAttribute("users", userService.findAll());
        model.addAttribute("orderedVouchers", voucherService.findAllOrdered());
        return "admin/admin";
    }

    @GetMapping("/manager")
    public String managerPanel(Model model) {
        model.addAttribute("allVouchers", voucherService.findAll(0, 100, "id").getContent());
        model.addAttribute("orderedVouchers", voucherService.findAllOrdered());
        return "manager/manager";
    }
}
