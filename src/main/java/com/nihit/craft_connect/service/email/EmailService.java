package com.nihit.craft_connect.service.email;

import org.springframework.stereotype.Service;

@Service
public interface EmailService {
    void sendOtpEmail(String toEmail, String otp);
}
