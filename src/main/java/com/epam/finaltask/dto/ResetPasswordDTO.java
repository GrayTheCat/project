package com.epam.finaltask.dto;

import com.epam.finaltask.vallidation.ValidPassword;
import lombok.Data;

@Data
public class ResetPasswordDTO {

    private String token;

    @ValidPassword
    private String password;
}