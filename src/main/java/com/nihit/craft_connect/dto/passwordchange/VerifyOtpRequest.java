package com.nihit.craft_connect.dto.passwordchange;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
public class VerifyOtpRequest {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String otp;
}