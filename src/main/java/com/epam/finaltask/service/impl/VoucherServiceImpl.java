package com.epam.finaltask.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.ResourceNotFoundException;
import com.epam.finaltask.exception.BusinessLogicException;
import com.epam.finaltask.model.*;
import com.epam.finaltask.model.enums.HotelType;
import com.epam.finaltask.model.enums.TourType;
import com.epam.finaltask.model.enums.TransferType;
import com.epam.finaltask.model.enums.VoucherStatus;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public VoucherDTO create(VoucherDTO voucherDTO) {
        Voucher voucher = modelMapper.map(voucherDTO, Voucher.class);
        voucher.setStatus(VoucherStatus.REGISTERED);
        voucher.setIsHot(false);
        return modelMapper.map(voucherRepository.save(voucher), VoucherDTO.class);
    }

    @Override
    @Transactional
    public VoucherDTO order(String id, String userId) {
        Voucher catalogVoucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }
        if (user.getBalance().compareTo(BigDecimal.valueOf(catalogVoucher.getPrice())) < 0) {
            throw new BusinessLogicException("Not enough funds on balance to order this tour.");
        }
        user.setBalance(user.getBalance().subtract(BigDecimal.valueOf(catalogVoucher.getPrice())));
        userRepository.save(user);
        Voucher userOrder = new Voucher();
        userOrder.setTitle(catalogVoucher.getTitle());
        userOrder.setDescription(catalogVoucher.getDescription());
        userOrder.setPrice(catalogVoucher.getPrice());
        userOrder.setTourType(catalogVoucher.getTourType());
        userOrder.setTransferType(catalogVoucher.getTransferType());
        userOrder.setHotelType(catalogVoucher.getHotelType());
        userOrder.setArrivalDate(catalogVoucher.getArrivalDate());
        userOrder.setEvictionDate(catalogVoucher.getEvictionDate());
        userOrder.setIsHot(catalogVoucher.getIsHot());
        userOrder.setUser(user);
        userOrder.setStatus(VoucherStatus.REGISTERED);
        return modelMapper.map(voucherRepository.save(userOrder), VoucherDTO.class);
    }

    @Override
    public VoucherDTO update(String id, VoucherDTO voucherDTO) {
        Voucher existingVoucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));

        modelMapper.map(voucherDTO, existingVoucher);
        existingVoucher.setId(UUID.fromString(id));

        return modelMapper.map(voucherRepository.save(existingVoucher), VoucherDTO.class);
    }

    @Override
    public void delete(String voucherId) {
        if (!voucherRepository.existsById(UUID.fromString(voucherId))) {
            throw new ResourceNotFoundException("Voucher not found");
        }
        voucherRepository.deleteById(UUID.fromString(voucherId));
    }

    @Override
    public VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));

        voucher.setIsHot(voucherDTO.getIsHot());
        return modelMapper.map(voucherRepository.save(voucher), VoucherDTO.class);
    }

    @Override
    public List<VoucherDTO> findAllByUserId(String userId) {
        return voucherRepository.findAllByUserId(UUID.fromString(userId)).stream()
                .map(v -> modelMapper.map(v, VoucherDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<VoucherDTO> findAllByTourType(TourType tourType) {
        return voucherRepository.findAllByTourType(tourType).stream()
                .map(v -> modelMapper.map(v, VoucherDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<VoucherDTO> findAllByTransferType(String transferType) {
        return voucherRepository.findAllByTransferType(TransferType.valueOf(transferType)).stream()
                .map(v -> modelMapper.map(v, VoucherDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<VoucherDTO> findAllByPrice(Double price) {
        return voucherRepository.findAllByPrice(price).stream()
                .map(v -> modelMapper.map(v, VoucherDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<VoucherDTO> findAllByHotelType(HotelType hotelType) {
        return voucherRepository.findAllByHotelType(hotelType).stream()
                .map(v -> modelMapper.map(v, VoucherDTO.class)).collect(Collectors.toList());
    }

    @Override
    public Page<VoucherDTO> findAll(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return voucherRepository.findAll(pageable)
                .map(v -> modelMapper.map(v, VoucherDTO.class));
    }

    @Override
    public Page<VoucherDTO> findFiltered(String search, Double maxPrice, String type, String transfer, String hotel, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "isHot").and(Sort.by(sortBy)));
        if (search != null && search.trim().isEmpty()) {
            search = null;
        }
        TourType tourTypeEnum = null;
        if (type != null && !type.trim().isEmpty()) {
            try { tourTypeEnum = TourType.valueOf(type); } catch (IllegalArgumentException e) {}
        }
        TransferType transferTypeEnum = null;
        if (transfer != null && !transfer.trim().isEmpty()) {
            if (transfer.equals("CAR")) transfer = "PRIVATE_CAR";
            try { transferTypeEnum = TransferType.valueOf(transfer); } catch (IllegalArgumentException e) {}
        }
        HotelType hotelTypeEnum = null;
        if (hotel != null && !hotel.trim().isEmpty()) {
            try { hotelTypeEnum = HotelType.valueOf(hotel); } catch (IllegalArgumentException e) {}
        }
        Page<Voucher> vouchers = voucherRepository.findFiltered(search, maxPrice, tourTypeEnum, transferTypeEnum, hotelTypeEnum, pageable);
        return vouchers.map(v -> modelMapper.map(v, VoucherDTO.class));
    }

    @Override
    @Transactional
    public void changeStatus(String id, String status, String reason) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));

        try {
            voucher.setStatus(VoucherStatus.valueOf(status));
            if (VoucherStatus.CANCELED.name().equals(status)) {
                voucher.setCancellationReason(reason);
            } else {
                voucher.setCancellationReason(null);
            }
            voucherRepository.save(voucher);
        } catch (IllegalArgumentException e) {
            throw new BusinessLogicException("Invalid voucher status: " + status);
        }
    }

    @Override
    public List<VoucherDTO> findAllOrdered() {
        return voucherRepository.findByUserNotNull().stream()
                .filter(v -> v.getStatus() != VoucherStatus.PAID && v.getStatus() != VoucherStatus.CANCELED)
                .map(v -> modelMapper.map(v, VoucherDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleHotStatus(String id) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
        voucher.setIsHot(!voucher.getIsHot());
        voucherRepository.save(voucher);
    }
}
