package com.nihit.craft_connect.service.payment.impl;


import com.nihit.craft_connect.config.EsewaSignatureUtil;
import com.nihit.craft_connect.dto.esewa.EsewaCallbackData;
import com.nihit.craft_connect.dto.esewa.EsewaConfig;
import com.nihit.craft_connect.dto.esewa.EsewaStatusCheckResponse;
import com.nihit.craft_connect.dto.payment.PaymentInitiationResult;

import com.nihit.craft_connect.entity.Order;
import com.nihit.craft_connect.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsewaPaymentServiceImpl implements com.nihit.craft_connect.service.payment.PaymentGatewayService {

    private final EsewaConfig esewaConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PaymentInitiationResult initiatePayment(Order order, Payment payment) {
        // no tax/service/delivery charges in your system currently — all zero
        String amount = String.valueOf(payment.getAmount());
        String taxAmount = "0";
        String totalAmount = String.valueOf(payment.getAmount());
        String transactionUuid = payment.getMerchantTxnId();
        String productCode = esewaConfig.getProductCode();

        // signed_field_names order MUST match the order fields appear in the signed message
        String signedFieldNames = "total_amount,transaction_uuid,product_code";
        String message = "total_amount=" + totalAmount
                + ",transaction_uuid=" + transactionUuid
                + ",product_code=" + productCode;
        String signature = EsewaSignatureUtil.generateSignature(message, esewaConfig.getSecretKey());

        Map<String, String> formFields = new LinkedHashMap<>();
        formFields.put("amount", amount);
        formFields.put("tax_amount", taxAmount);
        formFields.put("total_amount", totalAmount);
        formFields.put("transaction_uuid", transactionUuid);
        formFields.put("product_code", productCode);
        formFields.put("product_service_charge", "0");
        formFields.put("product_delivery_charge", "0");
        formFields.put("success_url", esewaConfig.getSuccessUrl());
        formFields.put("failure_url", esewaConfig.getFailureUrl());
        formFields.put("signed_field_names", signedFieldNames);
        formFields.put("signature", signature);

        // unlike Khalti, eSewa's "redirect" is actually a form the frontend must POST —
        // there's no separate pidx/payment_url returned by an API call first
        return new PaymentInitiationResult(esewaConfig.getFormUrl(), formFields);
    }

    @Override
    public boolean verifyPayment(String merchantTxnId, Map<String, String> callbackParams) {
        // callbackParams contains the raw base64 "data" param from the success redirect
        String encodedData = callbackParams.get("data");
        if (encodedData == null) {
            log.warn("eSewa callback missing 'data' param for txn={}", merchantTxnId);
            return false;
        }

        EsewaCallbackData callbackData;
        try {
            String decoded = new String(Base64.getDecoder().decode(encodedData), StandardCharsets.UTF_8);
            callbackData = objectMapper.readValue(decoded, EsewaCallbackData.class);
        } catch (Exception e) {
            log.error("Failed to decode/parse eSewa callback data", e);
            return false;
        }

        // 1. verify the signature eSewa sent back matches what we'd compute ourselves
        if (!verifyCallbackSignature(callbackData)) {
            log.warn("eSewa callback signature mismatch for txn={}", merchantTxnId);
            return false;
        }

        if (!"COMPLETE".equalsIgnoreCase(callbackData.getStatus())) {
            return false;
        }

        // 2. never trust the redirect alone — confirm independently via the status-check API
        return confirmViaStatusCheck(callbackData.getTransaction_uuid(), callbackData.getTotal_amount());
    }

    private boolean verifyCallbackSignature(EsewaCallbackData data) {
        if (data.getSigned_field_names() == null || data.getSignature() == null) {
            return false;
        }
        // rebuild the message in the exact order signed_field_names specifies
        String[] fields = data.getSigned_field_names().split(",");
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].trim();
            String value = switch (field) {
                case "transaction_code" -> data.getTransaction_code();
                case "status" -> data.getStatus();
                case "total_amount" -> String.valueOf(data.getTotal_amount());
                case "transaction_uuid" -> data.getTransaction_uuid();
                case "product_code" -> data.getProduct_code();
                case "signed_field_names" -> data.getSigned_field_names();
                default -> "";
            };
            message.append(field).append("=").append(value);
            if (i < fields.length - 1) message.append(",");
        }

        String expectedSignature = EsewaSignatureUtil.generateSignature(message.toString(), esewaConfig.getSecretKey());
        return expectedSignature.equals(data.getSignature());
    }

    private boolean confirmViaStatusCheck(String transactionUuid, Double totalAmount) {
        String url = UriComponentsBuilder.fromUri(URI.create(esewaConfig.getStatusCheckUrl()))
                .queryParam("product_code", esewaConfig.getProductCode())
                .queryParam("total_amount", totalAmount)
                .queryParam("transaction_uuid", transactionUuid)
                .toUriString();

        try {
            EsewaStatusCheckResponse response = restTemplate.getForObject(url, EsewaStatusCheckResponse.class);
            return response != null && "COMPLETE".equalsIgnoreCase(response.getStatus());
        } catch (Exception e) {
            log.error("eSewa status-check call failed for txn={}", transactionUuid, e);
            return false;
        }
    }
}