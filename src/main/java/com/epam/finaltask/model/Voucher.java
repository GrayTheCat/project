package com.epam.finaltask.model;

import java.time.LocalDate;
import java.util.UUID;

import com.epam.finaltask.model.enums.HotelType;
import com.epam.finaltask.model.enums.TourType;
import com.epam.finaltask.model.enums.TransferType;
import com.epam.finaltask.model.enums.VoucherStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String description;

    private Double price;

    private String cancellationReason;

    @Enumerated(EnumType.STRING)
    private TourType tourType;

    @Enumerated(EnumType.STRING)
    private TransferType transferType;

    @Enumerated(EnumType.STRING)
    private HotelType hotelType;

    @Enumerated(EnumType.STRING)
    private VoucherStatus status;

    private LocalDate arrivalDate;

    private LocalDate evictionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private boolean isHot;

    public Boolean getIsHot() {
        return this.isHot;
    }

    public void setIsHot(Boolean isHot) {
        this.isHot = isHot != null && isHot;
    }
}
