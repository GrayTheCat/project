package com.epam.finaltask.dto;

import java.math.BigDecimal;

import com.epam.finaltask.vallidation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String id;

    @NotBlank(message = "{user.username.notblank}")
    @Size(min = 3, max = 50, message = "{user.username.size}")
    private String username;

    @NotBlank(message = "{user.firstname.notblank}")
    private String firstName;

    @NotBlank(message = "{user.lastname.notblank}")
    private String lastName;

    @NotBlank(message = "{user.email.notblank}")
    private String email;

    @NotBlank(message = "{user.password.notblank}")
    @ValidPassword
    private String password;

    private String role;

    private String phoneNumber;

    private BigDecimal balance;

    private boolean active;
}
