package com.nihit.craft_connect.dto.passwordchange;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
public class ForgotPasswordRequest {
    @NotBlank
    @Email
    private String email;
}
