package com.nihit.craft_connect.service.password;

import com.nihit.craft_connect.dto.passwordchange.ChangePasswordRequest;
import com.nihit.craft_connect.dto.passwordchange.ResetPasswordRequest;
import org.springframework.stereotype.Service;

@Service
public interface PasswordService {
    void changePassword(ChangePasswordRequest request);
    void requestPasswordResetOtp(String email);
    void verifyOtp(String email, String otp);
    void resetPassword(ResetPasswordRequest request);
}
