package com.nihit.craft_connect.service.payment.impl;

import com.nihit.craft_connect.dto.khalti.*;
import com.nihit.craft_connect.dto.payment.PaymentInitiationResult;
import com.nihit.craft_connect.entity.Order;
import com.nihit.craft_connect.entity.Payment;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.service.payment.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KhaltiPaymentServiceImpl implements PaymentGatewayService {

    private final KhaltiConfig khaltiConfig;
    private final RestTemplate restTemplate;

    @Override
    public PaymentInitiationResult initiatePayment(Order order, Payment payment) {
        KhaltiInitiateRequest request = new KhaltiInitiateRequest();
        request.setReturn_url(khaltiConfig.getReturnUrl());
        request.setWebsite_url(khaltiConfig.getWebsiteUrl());
        // amount must be in paisa — Payment.amount is stored in rupees
        request.setAmount(Math.round(payment.getAmount() * 100));
        request.setPurchase_order_id(payment.getMerchantTxnId());
        request.setPurchase_order_name("Order #" + order.getId());

        KhaltiCustomerInfo customerInfo = new KhaltiCustomerInfo();
        customerInfo.setName(order.getUser().getFirstName() + " " + order.getUser().getLastName());   // adjust getter to whatever your User entity exposes
        customerInfo.setEmail(order.getUser().getEmail());
        customerInfo.setPhone(order.getUser().getMobileNumber());
        request.setCustomer_info(customerInfo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "key " + khaltiConfig.getSecretKey());

        System.out.println("secret key: "+ khaltiConfig.getSecretKey());

        HttpEntity<KhaltiInitiateRequest> entity = new HttpEntity<>(request, headers);

        KhaltiInitiateResponse response;
        try {
            response = restTemplate.postForObject(
                    khaltiConfig.getBaseUrl() + "/epayment/initiate/",
                    entity,
                    KhaltiInitiateResponse.class
            );
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Khalti initiate failed: {}", ex.getResponseBodyAsString());
            throw new AppException("Failed to initiate Khalti payment. Please try again.");
        }

        if (response == null || response.getPayment_url() == null) {
            String detail = response != null ? response.getDetail() : "No response from Khalti";
            log.error("Khalti initiate returned no payment_url: {}", detail);
            throw new AppException("Failed to initiate Khalti payment. Please try again.");
        }

        // save pidx immediately — needed for lookup during callback
        payment.setGatewayTxnId(response.getPidx());

        return new PaymentInitiationResult(response.getPayment_url(), Map.of());
    }

    @Override
    public boolean verifyPayment(String merchantTxnId, Map<String, String> callbackParams) {
        String pidx = callbackParams.get("pidx");
        if (pidx == null || pidx.isBlank()) {
            log.warn("Khalti callback missing pidx for merchantTxnId={}", merchantTxnId);
            return false;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "key " + khaltiConfig.getSecretKey());

        HttpEntity<KhaltiLookupRequest> entity = new HttpEntity<>(new KhaltiLookupRequest(pidx), headers);

        KhaltiLookupResponse lookup;
        try {
            lookup = restTemplate.postForObject(
                    khaltiConfig.getBaseUrl() + "/epayment/lookup/",
                    entity,
                    KhaltiLookupResponse.class
            );
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("Khalti lookup failed for pidx={}: {}", pidx, ex.getResponseBodyAsString());
            return false;
        }

        // per Khalti docs: only "Completed" is a confirmed success.
        // Pending/Initiated/Refunded/Expired/User canceled must all be treated as not-yet-paid or failed.
        return lookup != null && "Completed".equalsIgnoreCase(lookup.getStatus());
    }
}
