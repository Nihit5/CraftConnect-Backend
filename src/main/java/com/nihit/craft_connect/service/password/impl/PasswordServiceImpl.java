package com.nihit.craft_connect.service.password.impl;

import com.nihit.craft_connect.config.UserDetailConfig;
import com.nihit.craft_connect.dto.passwordchange.ChangePasswordRequest;
import com.nihit.craft_connect.dto.passwordchange.ResetPasswordRequest;
import com.nihit.craft_connect.entity.PasswordResetOtp;
import com.nihit.craft_connect.entity.User;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.repository.PasswordResetOtpRepository;
import com.nihit.craft_connect.repository.UserRepository;
import com.nihit.craft_connect.service.email.EmailService;
import com.nihit.craft_connect.service.password.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {
    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserDetailConfig userDetailConfig;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALID_MINUTES = 10;

    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest request) {
        User user = userRepository.findById(userDetailConfig.getLoggedInUserId())
                .orElseThrow(() -> new AppException("User not found."));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException("Old password is incorrect.");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AppException("New password must be different from old password.");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException("New password and confirm password do not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void requestPasswordResetOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("No account found with this email."));

        String otp = generateOtp();

        PasswordResetOtp resetOtp = new PasswordResetOtp();
        resetOtp.setEmail(user.getEmail());
        resetOtp.setOtp(passwordEncoder.encode(otp));
        resetOtp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_VALID_MINUTES));
        resetOtp.setUsed(false);
        otpRepository.save(resetOtp);

        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    @Transactional(readOnly = true)
    @Override
    public void verifyOtp(String email, String otp) {
        PasswordResetOtp record = otpRepository
                .findTopByEmailAndUsedFalseOrderByIdDesc(email)
                .orElseThrow(() -> new AppException("No OTP request found for this email."));

        validateOtp(record, otp);
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException("New password and confirm password do not match.");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("No account found with this email."));

        PasswordResetOtp record = otpRepository
                .findTopByEmailAndUsedFalseOrderByIdDesc(request.getEmail())
                .orElseThrow(() -> new AppException("No OTP request found for this email."));

        validateOtp(record, request.getOtp());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        record.setUsed(true);
        otpRepository.save(record);
    }

    private void validateOtp(PasswordResetOtp record, String rawOtp) {
        if (record.isUsed()) {
            throw new AppException("This OTP has already been used.");
        }
        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException("OTP has expired. Please request a new one.");
        }
        if (!passwordEncoder.matches(rawOtp, record.getOtp())) {
            throw new AppException("Invalid OTP.");
        }
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
