package com.epam.finaltask.service.impl;

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
import org.springframework.stereotype.Service;

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
        voucher.setHot(false);
        return modelMapper.map(voucherRepository.save(voucher), VoucherDTO.class);
    }

    @Override
    public VoucherDTO order(String id, String userId) {
        Voucher voucher = voucherRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (voucher.getStatus() != VoucherStatus.REGISTERED) {
            throw new BusinessLogicException("You can only order 'REGISTERED' vouchers.");
        }

        voucher.setUser(user);
        voucher.setStatus(VoucherStatus.PAID);
        return modelMapper.map(voucherRepository.save(voucher), VoucherDTO.class);
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

        voucher.setHot(voucherDTO.getIsHot());
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
    public List<VoucherDTO> findAll() {
        return voucherRepository.findAllByOrderByIsHotDesc().stream()
                .map(v -> modelMapper.map(v, VoucherDTO.class)).collect(Collectors.toList());
    }
}
