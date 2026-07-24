package com.nihit.craft_connect.service.payment.impl;

import com.nihit.craft_connect.dto.esewa.EsewaCallbackData;
import com.nihit.craft_connect.entity.Order;
import com.nihit.craft_connect.entity.Payment;
import com.nihit.craft_connect.enums.OrderStatus;
import com.nihit.craft_connect.enums.PaymentStatus;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.repository.OrderRepository;
import com.nihit.craft_connect.repository.PaymentRepository;
import com.nihit.craft_connect.service.payment.PaymentCallbackService;
import com.nihit.craft_connect.service.payment.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackServiceImpl implements PaymentCallbackService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGatewayServiceFactory paymentGatewayServiceFactory;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Long handleKhaltiCallback(String purchaseOrderId, String pidx) {
        Payment payment = paymentRepository.findByMerchantTxnId(purchaseOrderId)
                .orElseThrow(() -> new AppException("Payment record not found."));

        Order order = payment.getOrder(); // still lazy, but we're inside a transaction now — safe to touch

        PaymentGatewayService khaltiService = paymentGatewayServiceFactory.getService(payment.getMethod());
        boolean verified = khaltiService.verifyPayment(payment.getMerchantTxnId(), Map.of("pidx", pidx));

        Timestamp now = new Timestamp(System.currentTimeMillis());

        if (verified) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setGatewayTxnId(pidx);
            payment.setModifiedDate(now);

            order.setStatus(OrderStatus.CONFIRMED);
            order.setModifiedDate(now);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setModifiedDate(now);

            order.setStatus(OrderStatus.PAYMENT_FAILED);
            order.setModifiedDate(now);
        }

        // both are managed entities in this persistence context — no explicit save() needed,
        // but calling it doesn't hurt and makes intent obvious
        paymentRepository.save(payment);
        orderRepository.save(order);

        return order.getId();
    }

    @Transactional
    public Long handleEsewaCallback(String rawData) {
        Map<String, String> params = Map.of("data", rawData);

        // decode just enough to find our own txn id before full verification
        String decoded = new String(Base64.getDecoder().decode(rawData), StandardCharsets.UTF_8);
        EsewaCallbackData callbackData;
        try {
            callbackData = objectMapper.readValue(decoded, EsewaCallbackData.class);
        } catch (Exception e) {
            throw new AppException("Invalid eSewa callback data.");
        }

        Payment payment = paymentRepository.findByMerchantTxnId(callbackData.getTransaction_uuid())
                .orElseThrow(() -> new AppException("Payment record not found."));
        Order order = payment.getOrder();

        PaymentGatewayService esewaService = paymentGatewayServiceFactory.getService(payment.getMethod());
        boolean verified = esewaService.verifyPayment(payment.getMerchantTxnId(), params);

        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (verified) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setGatewayTxnId(callbackData.getTransaction_code());
            order.setStatus(OrderStatus.CONFIRMED);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
        }
        payment.setModifiedDate(now);
        order.setModifiedDate(now);
        paymentRepository.save(payment);
        orderRepository.save(order);

        return order.getId();
    }

    @Transactional
    public Long handleEsewaFailure(String rawData) {
        if (rawData == null || rawData.isBlank()) {
            // eSewa gave us nothing to identify the transaction — nothing to update
            log.warn("eSewa failure callback received with no data param");
            return null;
        }

        EsewaCallbackData callbackData;
        try {
            String decoded = new String(Base64.getDecoder().decode(rawData), StandardCharsets.UTF_8);
            callbackData = objectMapper.readValue(decoded, EsewaCallbackData.class);
        } catch (Exception e) {
            log.warn("eSewa failure callback data was unparseable", e);
            return null;
        }

        Payment payment = paymentRepository.findByMerchantTxnId(callbackData.getTransaction_uuid())
                .orElse(null);
        if (payment == null) {
            return null;
        }

        Order order = payment.getOrder();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        payment.setStatus(PaymentStatus.FAILED);
        payment.setModifiedDate(now);

        order.setStatus(OrderStatus.PAYMENT_FAILED);
        order.setModifiedDate(now);

        paymentRepository.save(payment);
        orderRepository.save(order);

        return order.getId();
    }
}
