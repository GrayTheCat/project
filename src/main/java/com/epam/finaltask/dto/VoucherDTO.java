package com.epam.finaltask.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDTO {

    private UUID id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Min(value = 0, message = "Price cannot be negative")
    private Double price;

    private String tourType;
    private String transferType;
    private String hotelType;
    private String status;

    private LocalDate arrivalDate;
    private LocalDate evictionDate;

    private Boolean isHot;
    private UserDTO user;
    private String cancellationReason;
}
