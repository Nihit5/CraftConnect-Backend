package com.nihit.craft_connect.controller.password;

import com.nihit.craft_connect.constants.SuccessConstants;
import com.nihit.craft_connect.controller.BaseController;
import com.nihit.craft_connect.dto.GlobalApiResponse;
import com.nihit.craft_connect.dto.passwordchange.ChangePasswordRequest;
import com.nihit.craft_connect.dto.passwordchange.ForgotPasswordRequest;
import com.nihit.craft_connect.dto.passwordchange.ResetPasswordRequest;
import com.nihit.craft_connect.dto.passwordchange.VerifyOtpRequest;
import com.nihit.craft_connect.service.password.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
public class PasswordController  extends BaseController {

    private final PasswordService passwordService;


    @PostMapping("/change")
    public ResponseEntity<GlobalApiResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        passwordService.changePassword(changePasswordRequest);
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_UPDATE, "Password"), null));
    }

    @PostMapping("/forgot/request-otp")
    public ResponseEntity<GlobalApiResponse> requestOtp(
            @Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        passwordService.requestPasswordResetOtp(forgotPasswordRequest.getEmail());
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_CREATE, "OTP"), null));
    }

    @PostMapping("/forgot/verify-otp")
    public ResponseEntity<GlobalApiResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        passwordService.verifyOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp());
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "OTP"), null));
    }

    @PostMapping("/forgot/reset")
    public ResponseEntity<GlobalApiResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        passwordService.resetPassword(resetPasswordRequest);
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_UPDATE, "Password"), null));
    }
}
