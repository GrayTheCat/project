package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.ApiResponse;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.enums.HotelType;
import com.epam.finaltask.model.enums.TourType;
import com.epam.finaltask.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherRestController {

    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VoucherDTO>>> getAllVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        return ResponseEntity.ok(new ApiResponse<>(null, null, voucherService.findAll(page, size, sortBy)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> getVouchersByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(new ApiResponse<>(null, null, voucherService.findAllByUserId(userId)));
    }

    @GetMapping("/com/epam/finaltask/filter/hotel")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> filterByHotelType(@RequestParam HotelType type) {
        return ResponseEntity.ok(new ApiResponse<>(null, null, voucherService.findAllByHotelType(type)));
    }

    @GetMapping("/com/epam/finaltask/filter/transfer")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> filterByTransferType(@RequestParam String type) {
        return ResponseEntity.ok(new ApiResponse<>(null, null, voucherService.findAllByTransferType(type)));
    }

    @GetMapping("/com/epam/finaltask/filter/tour")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> filterByTourType(@RequestParam TourType type) {
        return ResponseEntity.ok(new ApiResponse<>(null, null, voucherService.findAllByTourType(type)));
    }

    @GetMapping("/filter/price")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> filterByPrice(@RequestParam Double maxPrice) {
        return ResponseEntity.ok(new ApiResponse<>(null, null, voucherService.findAllByPrice(maxPrice)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createVoucher(@Valid @RequestBody VoucherDTO voucherDTO) {
        voucherService.create(voucherDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("OK", "Voucher is successfully created", null));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateVoucher(@PathVariable String id, @Valid @RequestBody VoucherDTO voucherDTO) {
        voucherService.update(id, voucherDTO);
        return ResponseEntity.ok(new ApiResponse<>("OK", "Voucher is successfully updated", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable String id) {
        voucherService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>("OK", String.format("Voucher with Id %s has been deleted", id), null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> changeHotStatus(@PathVariable String id, @RequestBody VoucherDTO voucherDTO) {
        voucherService.changeHotStatus(id, voucherDTO);
        return ResponseEntity.ok(new ApiResponse<>("OK", "Voucher status is successfully changed", null));
    }

    @PostMapping("/{id}/order")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<VoucherDTO>> orderVoucher(
            @PathVariable String id,
            @RequestParam String userId) {
        return ResponseEntity.ok(
                new ApiResponse<>("OK", "Voucher ordered successfully", voucherService.order(id, userId))
        );
    }
}
