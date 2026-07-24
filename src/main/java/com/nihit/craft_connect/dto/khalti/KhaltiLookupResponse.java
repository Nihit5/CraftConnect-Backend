package com.nihit.craft_connect.dto.khalti;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KhaltiLookupResponse {
    private String pidx;
    private Long total_amount;
    private String status;            // Completed | Pending | Initiated | Refunded | Expired | User canceled
    private String transaction_id;
    private Integer fee;
    private Boolean refunded;
}