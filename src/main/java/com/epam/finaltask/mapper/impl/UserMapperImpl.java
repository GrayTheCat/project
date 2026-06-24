package com.epam.finaltask.mapper.impl;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapperImpl implements UserMapper {

    private final ModelMapper modelMapper;

    @Override
    public User toUser(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        return modelMapper.map(userDTO, User.class);
    }

    @Override
    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        return modelMapper.map(user, UserDTO.class);
    }
}
