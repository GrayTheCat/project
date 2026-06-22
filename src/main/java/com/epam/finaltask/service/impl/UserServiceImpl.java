package com.epam.finaltask.service.impl;

import java.math.BigDecimal;
import java.util.UUID;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.ResourceNotFoundException;
import com.epam.finaltask.exception.UserAlreadyExistsException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.enums.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO register(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username '" + userDTO.getUsername() + "' is already taken.");
        }

        User user = userMapper.toUser(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(Role.USER);
        user.setActive(true);
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }

        return userMapper.toUserDTO(userRepository.save(user));
    }

    @Override
    public UserDTO updateUser(String username, UserDTO userDTO) {
        User existingUser = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getBalance() != null) {
            existingUser.setBalance(userDTO.getBalance());
        }

        return userMapper.toUserDTO(userRepository.save(existingUser));
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toUserDTO(user);
    }

    @Override
    public UserDTO changeAccountStatus(UserDTO userDTO) {
        userRepository.findById(UUID.fromString(userDTO.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User user = userMapper.toUser(userDTO);
        return userMapper.toUserDTO(userRepository.save(user));
    }

    @Override
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toUserDTO(user);
    }

}
