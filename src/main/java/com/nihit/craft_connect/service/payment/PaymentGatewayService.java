package com.nihit.craft_connect.service.payment;

import com.nihit.craft_connect.dto.payment.PaymentInitiationResult;
import com.nihit.craft_connect.entity.Order;
import com.nihit.craft_connect.entity.Payment;

public interface PaymentGatewayService {
    PaymentInitiationResult initiatePayment(Order order, Payment payment);

    // called from your callback/verification controller after redirect back from gateway
    boolean verifyPayment(String merchantTxnId, java.util.Map<String, String> callbackParams);
}
