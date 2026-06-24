package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.ResourceNotFoundException;
import com.epam.finaltask.exception.UserAlreadyExistsException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ModelMapper modelMapper;

    @InjectMocks private UserServiceImpl userService;

    @Test
    void register_UserExists_ThrowsException() {
        UserDTO dto = new UserDTO();
        dto.setUsername("existingUser");
        when(userRepository.existsByUsername("existingUser")).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> userService.register(dto));
    }

    @Test
    void register_NewUser_Success() {
        UserDTO dto = new UserDTO();
        dto.setUsername("newUser");
        dto.setPassword("pass");
        User user = new User();
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userMapper.toUser(dto)).thenReturn(user);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(dto);
        UserDTO result = userService.register(dto);
        assertEquals("encodedPass", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_UserNotFound_ThrowsException() {
        when(userRepository.findUserByUsername("user")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser("user", new UserDTO()));
    }

    @Test
    void updateUser_Success() {
        User existingUser = new User();
        UserDTO dto = new UserDTO();
        dto.setPhoneNumber("123");
        dto.setBalance(BigDecimal.TEN);
        when(userRepository.findUserByUsername("user")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toUserDTO(existingUser)).thenReturn(dto);
        userService.updateUser("user", dto);
        assertEquals("123", existingUser.getPhoneNumber());
        verify(userRepository).save(existingUser);
    }

    @Test
    void changeAccountStatus_UserNotFound_ThrowsException() {
        UserDTO dto = new UserDTO();
        dto.setId(UUID.randomUUID().toString());
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.changeAccountStatus(dto));
    }

    @Test
    void register_NullBalance_SetsZero() {
        UserDTO dto = new UserDTO();
        dto.setUsername("user");
        dto.setPassword("pass");
        User user = new User();
        user.setBalance(null);
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userMapper.toUser(dto)).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toUserDTO(any())).thenReturn(dto);
        userService.register(dto);
        assertEquals(BigDecimal.ZERO, user.getBalance());
    }

    @Test
    void updateUser_WithBalance_UpdatesBalance() {
        User existingUser = new User();
        existingUser.setBalance(BigDecimal.ZERO);
        UserDTO dto = new UserDTO();
        dto.setBalance(BigDecimal.TEN);
        when(userRepository.findUserByUsername("user")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenReturn(existingUser);
        when(userMapper.toUserDTO(any())).thenReturn(dto);
        userService.updateUser("user", dto);
        assertEquals(BigDecimal.TEN, existingUser.getBalance());
    }

    @Test
    void getUserByUsername_Success() {
        User user = new User();
        when(userRepository.findUserByUsername("test")).thenReturn(Optional.of(user));
        when(userMapper.toUserDTO(user)).thenReturn(new UserDTO());
        userService.getUserByUsername("test");
        verify(userMapper).toUserDTO(user);
    }

    @Test
    void changeAccountStatus_Success() {
        UserDTO dto = new UserDTO();
        dto.setId(UUID.randomUUID().toString());
        User user = new User();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(userMapper.toUser(dto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(dto);
        userService.changeAccountStatus(dto);
        verify(userRepository).save(user);
    }

    @Test
    void findAll_Success() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(new User()));
        when(modelMapper.map(any(), eq(UserDTO.class))).thenReturn(new UserDTO());
        userService.findAll();
        verify(userRepository).findAll();
    }

    @Test
    void register_WhenBalanceIsNull_SetsBalanceToZero() {
        UserDTO dto = new UserDTO();
        dto.setUsername("testUser");
        User user = new User();
        user.setBalance(null);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userMapper.toUser(dto)).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toUserDTO(any())).thenReturn(dto);
        userService.register(dto);
        assertEquals(BigDecimal.ZERO, user.getBalance());
    }

    @Test
    void register_WhenBalanceIsSet_KeepsExistingBalance() {
        UserDTO dto = new UserDTO();
        User user = new User();
        user.setBalance(new BigDecimal("50.00"));
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userMapper.toUser(dto)).thenReturn(user);
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toUserDTO(any())).thenReturn(dto);
        userService.register(dto);
        assertEquals(new BigDecimal("50.00"), user.getBalance());
    }

    @Test
    void updateUser_WhenBalanceProvided_UpdatesBalance() {
        User existingUser = new User();
        UserDTO dto = new UserDTO();
        dto.setBalance(new BigDecimal("100.00"));
        when(userRepository.findUserByUsername("user")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenReturn(existingUser);
        when(userMapper.toUserDTO(any())).thenReturn(dto);
        userService.updateUser("user", dto);
        assertEquals(new BigDecimal("100.00"), existingUser.getBalance());
    }

    @Test
    void updateUser_WhenBalanceIsNull_DoesNotUpdateBalance() {
        BigDecimal oldBalance = new BigDecimal("20.00");
        User existingUser = new User();
        existingUser.setBalance(oldBalance);
        UserDTO dto = new UserDTO();
        dto.setBalance(null);
        when(userRepository.findUserByUsername("user")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenReturn(existingUser);
        when(userMapper.toUserDTO(any())).thenReturn(dto);
        userService.updateUser("user", dto);
        assertEquals(oldBalance, existingUser.getBalance());
    }
}
