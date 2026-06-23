package com.epam.finaltask.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.epam.finaltask.model.enums.HotelType;
import com.epam.finaltask.model.enums.TourType;
import com.epam.finaltask.model.enums.TransferType;
import com.epam.finaltask.model.Voucher;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoucherRepository extends JpaRepository<Voucher, UUID> {
    Page<Voucher> findAll(Pageable pageable);
    List<Voucher> findAllByUserId(UUID userId);
    List<Voucher> findAllByTourType(TourType tourType);
    List<Voucher> findAllByTransferType(TransferType transferType);
    List<Voucher> findAllByPrice(Double price);
    List<Voucher> findAllByHotelType(HotelType hotelType);
    List<Voucher> findAllByOrderByIsHotDesc();
    @Query("SELECT v FROM Voucher v WHERE " +
            "(:search IS NULL OR LOWER(v.title) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))) AND " +
            "(:maxPrice IS NULL OR v.price <= :maxPrice) AND " +
            "(:tourType IS NULL OR v.tourType = :tourType) AND " +
            "(:transferType IS NULL OR v.transferType = :transferType) AND " +
            "(:hotelType IS NULL OR v.hotelType = :hotelType) AND " +
            "v.user IS NULL")
    Page<Voucher> findFiltered(
            @Param("search") String search,
            @Param("maxPrice") Double maxPrice,
            @Param("tourType") TourType tourType,
            @Param("transferType") TransferType transferType,
            @Param("hotelType") HotelType hotelType,
            Pageable pageable
    );
    List<Voucher> findByUserNotNull();
}
