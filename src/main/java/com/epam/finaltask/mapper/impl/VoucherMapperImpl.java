package com.epam.finaltask.mapper.impl;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.Voucher;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VoucherMapperImpl implements VoucherMapper {

    private final ModelMapper modelMapper;

    @Override
    public Voucher toVoucher(VoucherDTO voucherDTO) {
        if (voucherDTO == null) {
            return null;
        }
        return modelMapper.map(voucherDTO, Voucher.class);
    }

    @Override
    public VoucherDTO toVoucherDTO(Voucher voucher) {
        if (voucher == null) {
            return null;
        }
        VoucherDTO dto = modelMapper.map(voucher, VoucherDTO.class);
        if (voucher.getUser() != null) {
            dto.getUser().setId(voucher.getUser().getId().toString());
        }
        return dto;
    }
}
