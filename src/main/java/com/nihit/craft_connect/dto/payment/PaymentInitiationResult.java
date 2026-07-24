package com.nihit.craft_connect.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Map;

@Getter
@AllArgsConstructor
public class PaymentInitiationResult {
    private String redirectUrl;
    private Map<String, String> formFields;
}
