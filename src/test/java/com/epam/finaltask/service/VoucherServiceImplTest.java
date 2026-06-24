package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.BusinessLogicException;
import com.epam.finaltask.exception.ResourceNotFoundException;
import com.epam.finaltask.model.User;
import com.epam.finaltask.model.Voucher;
import com.epam.finaltask.model.enums.HotelType;
import com.epam.finaltask.model.enums.TourType;
import com.epam.finaltask.model.enums.TransferType;
import com.epam.finaltask.model.enums.VoucherStatus;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;
import com.epam.finaltask.service.impl.VoucherServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherServiceImplTest {

    @Mock private VoucherRepository voucherRepository;
    @Mock private UserRepository userRepository;
    @Mock private ModelMapper modelMapper;

    @InjectMocks private VoucherServiceImpl voucherService;

    @Test
    void order_NotEnoughFunds_ThrowsException() {
        Voucher catalog = new Voucher();
        catalog.setPrice(100.0);
        User user = new User();
        user.setBalance(BigDecimal.valueOf(50));
        when(voucherRepository.findById(any())).thenReturn(Optional.of(catalog));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        assertThrows(BusinessLogicException.class, () ->
                voucherService.order(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
    }

    @Test
    void validateVoucherData_InvalidDates_ThrowsException() {
        VoucherDTO dto = new VoucherDTO();
        dto.setPrice(100.0);
        dto.setArrivalDate(LocalDate.now().plusDays(5));
        dto.setEvictionDate(LocalDate.now().plusDays(2));
        assertThrows(BusinessLogicException.class, () -> voucherService.create(dto));
    }

    @Test
    void changeStatus_ToCanceled_SetsReason() {
        Voucher voucher = new Voucher();
        when(voucherRepository.findById(any())).thenReturn(Optional.of(voucher));
        voucherService.changeStatus(UUID.randomUUID().toString(), "CANCELED", "Too expensive");
        assertEquals(VoucherStatus.CANCELED, voucher.getStatus());
        assertEquals("Too expensive", voucher.getCancellationReason());
    }

    @Test
    void changeStatus_InvalidStatus_ThrowsException() {
        Voucher voucher = new Voucher();
        when(voucherRepository.findById(any())).thenReturn(Optional.of(voucher));
        assertThrows(BusinessLogicException.class, () ->
                voucherService.changeStatus(UUID.randomUUID().toString(), "INVALID_STATUS", null));
    }

    @Test
    void toggleHotStatus_Success() {
        Voucher voucher = new Voucher();
        voucher.setIsHot(false);
        when(voucherRepository.findById(any())).thenReturn(Optional.of(voucher));
        voucherService.toggleHotStatus(UUID.randomUUID().toString());
        assertTrue(voucher.getIsHot());
        verify(voucherRepository).save(voucher);
    }

    @Test
    void order_Success_InitializesZeroBalance() {
        Voucher catalog = new Voucher();
        catalog.setPrice(50.0);
        User user = new User();
        user.setBalance(null);
        when(voucherRepository.findById(any())).thenReturn(Optional.of(catalog));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        user.setBalance(BigDecimal.valueOf(100.0));
        when(voucherRepository.save(any())).thenReturn(new Voucher());
        when(modelMapper.map(any(), eq(VoucherDTO.class))).thenReturn(new VoucherDTO());
        voucherService.order(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        assertEquals(new BigDecimal("50.0"), user.getBalance());
        verify(userRepository).save(user);
    }

    @Test
    void findAllByUserId_ReturnsList() {
        when(voucherRepository.findAllByUserId(any())).thenReturn(List.of(new Voucher()));
        when(modelMapper.map(any(), eq(VoucherDTO.class))).thenReturn(new VoucherDTO());
        List<VoucherDTO> result = voucherService.findAllByUserId(UUID.randomUUID().toString());
        assertFalse(result.isEmpty());
    }

    @Test
    void findAllByTourType_ReturnsList() {
        when(voucherRepository.findAllByTourType(any())).thenReturn(List.of(new Voucher()));
        when(modelMapper.map(any(), eq(VoucherDTO.class))).thenReturn(new VoucherDTO());
        voucherService.findAllByTourType(TourType.LEISURE);
        verify(voucherRepository).findAllByTourType(TourType.LEISURE);
    }

    @Test
    void findAllByTransferType_ReturnsList() {
        when(voucherRepository.findAllByTransferType(any())).thenReturn(List.of(new Voucher()));
        when(modelMapper.map(any(), eq(VoucherDTO.class))).thenReturn(new VoucherDTO());
        voucherService.findAllByTransferType("BUS");
        verify(voucherRepository).findAllByTransferType(TransferType.BUS);
    }

    @Test
    void validateVoucherData_PriceNullOrNegative_ThrowsException() {
        VoucherDTO dto = new VoucherDTO();
        dto.setPrice(null);
        assertThrows(BusinessLogicException.class, () -> voucherService.create(dto));
        dto.setPrice(0.0);
        assertThrows(BusinessLogicException.class, () -> voucherService.create(dto));
    }

    @Test
    void validateVoucherData_ArrivalAfterEviction_ThrowsException() {
        VoucherDTO dto = new VoucherDTO();
        dto.setPrice(100.0);
        dto.setArrivalDate(LocalDate.now().plusDays(5));
        dto.setEvictionDate(LocalDate.now().plusDays(2));
        assertThrows(BusinessLogicException.class, () -> voucherService.create(dto));
    }

    @Test
    void validateVoucherData_PastArrivalForNewTour_ThrowsException() {
        VoucherDTO dto = new VoucherDTO();
        dto.setId(null);
        dto.setPrice(100.0);
        dto.setArrivalDate(LocalDate.now().minusDays(1));
        dto.setEvictionDate(LocalDate.now().plusDays(5));
        assertThrows(BusinessLogicException.class, () -> voucherService.create(dto));
    }

    @Test
    void order_NullBalance_SetsBalanceToZero() {
        Voucher catalogVoucher = new Voucher();
        catalogVoucher.setPrice(0.0);
        User user = new User();
        user.setBalance(null);
        when(voucherRepository.findById(any())).thenReturn(Optional.of(catalogVoucher));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(new Voucher());
        when(modelMapper.map(any(), eq(VoucherDTO.class))).thenReturn(new VoucherDTO());
        voucherService.order(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        assertEquals(0, BigDecimal.ZERO.compareTo(user.getBalance()));
        verify(userRepository).save(user);
    }

    @Test
    void update_Success() {
        String id = UUID.randomUUID().toString();
        VoucherDTO dto = new VoucherDTO();
        dto.setPrice(100.0);
        dto.setArrivalDate(LocalDate.now().plusDays(1));
        dto.setEvictionDate(LocalDate.now().plusDays(5));
        Voucher existingVoucher = new Voucher();
        when(voucherRepository.findById(any())).thenReturn(Optional.of(existingVoucher));
        doNothing().when(modelMapper).map(any(VoucherDTO.class), any(Voucher.class));
        when(voucherRepository.save(any())).thenReturn(existingVoucher);
        when(modelMapper.map(any(Voucher.class), eq(VoucherDTO.class))).thenReturn(dto);
        voucherService.update(id, dto);
        verify(voucherRepository).save(existingVoucher);
    }

    @Test
    void delete_VoucherExists_Success() {
        String id = UUID.randomUUID().toString();
        when(voucherRepository.existsById(any())).thenReturn(true);
        voucherService.delete(id);
        verify(voucherRepository).deleteById(any());
    }

    @Test
    void delete_VoucherNotExists_ThrowsException() {
        String id = UUID.randomUUID().toString();
        when(voucherRepository.existsById(any())).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> voucherService.delete(id));
    }

    @Test
    void changeHotStatus_Success() {
        String id = UUID.randomUUID().toString();
        VoucherDTO dto = new VoucherDTO();
        dto.setIsHot(true);
        Voucher voucher = new Voucher();
        when(voucherRepository.findById(any())).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any())).thenReturn(voucher);
        voucherService.changeHotStatus(id, dto);
        assertTrue(voucher.getIsHot());
        verify(voucherRepository).save(voucher);
    }

    @Test
    void findAllByPrice_ReturnsList() {
        when(voucherRepository.findAllByPrice(100.0)).thenReturn(List.of(new Voucher()));
        voucherService.findAllByPrice(100.0);
        verify(voucherRepository).findAllByPrice(100.0);
    }

    @Test
    void findAllByHotelType_ReturnsList() {
        when(voucherRepository.findAllByHotelType(HotelType.FIVE_STARS)).thenReturn(List.of(new Voucher()));
        voucherService.findAllByHotelType(HotelType.FIVE_STARS);
        verify(voucherRepository).findAllByHotelType(HotelType.FIVE_STARS);
    }

    @Test
    void findAll_ReturnsPage() {
        org.springframework.data.domain.Page<Voucher> page = new org.springframework.data.domain.PageImpl<>(List.of(new Voucher()));
        when(voucherRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
        voucherService.findAll(0, 10, "id");
        verify(voucherRepository).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void findFiltered_CoversAllParsingBranches() {
        org.springframework.data.domain.Page<Voucher> page = new org.springframework.data.domain.PageImpl<>(List.of(new Voucher()));
        when(voucherRepository.findFiltered(any(), any(), any(), any(), any(), any())).thenReturn(page);
        voucherService.findFiltered("   ", 1000.0, "INVALID_TYPE", "CAR", "INVALID_HOTEL", 0, 10, "price");
        verify(voucherRepository).findFiltered(
                isNull(), eq(1000.0), isNull(), eq(TransferType.PRIVATE_CAR), isNull(), any()
        );
    }

    @Test
    void changeStatus_WhenCanceled_SetsReason() {
        Voucher voucher = new Voucher();
        when(voucherRepository.findById(any())).thenReturn(Optional.of(voucher));
        voucherService.changeStatus(UUID.randomUUID().toString(), "CANCELED", "User request");
        assertEquals(VoucherStatus.CANCELED, voucher.getStatus());
        assertEquals("User request", voucher.getCancellationReason());
    }

    @Test
    void changeStatus_WhenNotCanceled_NullifiesReason() {
        Voucher voucher = new Voucher();
        voucher.setCancellationReason("Old reason");
        when(voucherRepository.findById(any())).thenReturn(Optional.of(voucher));
        voucherService.changeStatus(UUID.randomUUID().toString(), "REGISTERED", "Some reason");
        assertEquals(VoucherStatus.REGISTERED, voucher.getStatus());
        assertNull(voucher.getCancellationReason());
    }

    @Test
    void findAllOrdered_FiltersOutPaidAndCanceled() {
        Voucher vRegistered = new Voucher(); vRegistered.setStatus(VoucherStatus.REGISTERED);
        Voucher vPaid = new Voucher(); vPaid.setStatus(VoucherStatus.PAID);
        Voucher vCanceled = new Voucher(); vCanceled.setStatus(VoucherStatus.CANCELED);
        when(voucherRepository.findByUserNotNull()).thenReturn(List.of(vRegistered, vPaid, vCanceled));
        when(modelMapper.map(vRegistered, VoucherDTO.class)).thenReturn(new VoucherDTO());
        List<VoucherDTO> result = voucherService.findAllOrdered();
        assertEquals(1, result.size());
        verify(modelMapper, times(1)).map(any(), eq(VoucherDTO.class));
    }

    @Test
    void findFiltered_WithBlankStringsAndCar_CoversBranches() {
        org.springframework.data.domain.Page<Voucher> page = new org.springframework.data.domain.PageImpl<>(List.of());
        when(voucherRepository.findFiltered(any(), any(), any(), any(), any(), any())).thenReturn(page);
        voucherService.findFiltered("   ", 1000.0, "   ", "CAR", "   ", 0, 10, "price");
        verify(voucherRepository).findFiltered(
                isNull(), eq(1000.0), isNull(), eq(TransferType.PRIVATE_CAR), isNull(), any()
        );
    }

    @Test
    void findFiltered_WithValidEnums_CoversBranches() {
        org.springframework.data.domain.Page<Voucher> page = new org.springframework.data.domain.PageImpl<>(List.of());
        when(voucherRepository.findFiltered(any(), any(), any(), any(), any(), any())).thenReturn(page);
        voucherService.findFiltered("test", 1000.0, "LEISURE", "PLANE", "FIVE_STARS", 0, 10, "price");
        verify(voucherRepository).findFiltered(
                eq("test"), eq(1000.0), eq(TourType.LEISURE), eq(TransferType.PLANE), eq(HotelType.FIVE_STARS), any()
        );
    }

    @Test
    void create_NullDates_SkipsDateValidationAndSucceeds() {
        VoucherDTO dto = new VoucherDTO();
        dto.setPrice(100.0);
        Voucher voucher = new Voucher();
        when(modelMapper.map(any(VoucherDTO.class), eq(Voucher.class))).thenReturn(voucher);
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);
        when(modelMapper.map(any(Voucher.class), eq(VoucherDTO.class))).thenReturn(dto);
        voucherService.create(dto);
        assertEquals(VoucherStatus.REGISTERED, voucher.getStatus());
        assertFalse(voucher.getIsHot());
        verify(voucherRepository).save(any(Voucher.class));
    }

    @Test
    void validateVoucherData_PastArrivalDateForNewTour_ThrowsException() {
        VoucherDTO dto = new VoucherDTO();
        dto.setId(null);
        dto.setPrice(100.0);
        dto.setArrivalDate(LocalDate.now().minusDays(1));
        dto.setEvictionDate(LocalDate.now().plusDays(5));
        assertThrows(BusinessLogicException.class, () -> voucherService.create(dto));
    }
}