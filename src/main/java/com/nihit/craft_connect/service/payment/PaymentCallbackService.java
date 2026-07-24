package com.nihit.craft_connect.service.payment;

import org.springframework.stereotype.Service;

@Service
public interface PaymentCallbackService {
    Long handleKhaltiCallback(String purchaseOrderId, String pidx);
    Long handleEsewaCallback(String rawData);
    Long handleEsewaFailure(String rawData);
}
