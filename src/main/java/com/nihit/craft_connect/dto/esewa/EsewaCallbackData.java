package com.nihit.craft_connect.dto.esewa;

import lombok.*;

@Getter
@Setter
public class EsewaCallbackData {
    private String transaction_code;
    private String status;
    private Double total_amount;
    private String transaction_uuid;
    private String product_code;
    private String signed_field_names;
    private String signature;
}
