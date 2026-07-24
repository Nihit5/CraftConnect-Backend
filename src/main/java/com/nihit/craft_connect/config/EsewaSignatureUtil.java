package com.nihit.craft_connect.config;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class EsewaSignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    // message must be built in the exact order signed_field_names specifies,
    // e.g. "total_amount=110,transaction_uuid=241028,product_code=EPAYTEST"
    public static String generateSignature(String message, String secretKey) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Failed to generate eSewa signature", e);
            throw new RuntimeException("Signature generation failed", e);
        }
    }
}