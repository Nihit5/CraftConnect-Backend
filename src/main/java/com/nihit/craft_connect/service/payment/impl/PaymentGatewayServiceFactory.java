package com.nihit.craft_connect.service.payment.impl;

import com.nihit.craft_connect.enums.PaymentMethod;
import com.nihit.craft_connect.service.payment.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentGatewayServiceFactory {
    private final EsewaPaymentServiceImpl esewaPaymentService;
    private final KhaltiPaymentServiceImpl khaltiPaymentService;

    public PaymentGatewayService getService(PaymentMethod method) {
        return switch (method) {
            case ESEWA -> esewaPaymentService;
            case KHALTI -> khaltiPaymentService;
            default -> throw new IllegalArgumentException("No gateway for " + method);
        };
    }
}
