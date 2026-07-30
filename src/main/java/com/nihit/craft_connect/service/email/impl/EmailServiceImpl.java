package com.nihit.craft_connect.service.email.impl;

import com.nihit.craft_connect.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your password reset OTP");
        message.setText("Your OTP for password reset is: " + otp
                + "\nThis OTP is valid for 10 minutes. If you did not request this, please ignore this email.");
        mailSender.send(message);
    }
}