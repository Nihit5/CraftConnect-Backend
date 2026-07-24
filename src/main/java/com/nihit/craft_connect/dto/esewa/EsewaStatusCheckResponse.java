package com.nihit.craft_connect.dto.esewa;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EsewaStatusCheckResponse {
    private String product_code;
    private String transaction_uuid;
    private Double total_amount;
    private String status;   // COMPLETE | PENDING | FULL_REFUND | PARTIAL_REFUND | AMBIGUOUS | NOT_FOUND | CANCELED
    private String ref_id;
}