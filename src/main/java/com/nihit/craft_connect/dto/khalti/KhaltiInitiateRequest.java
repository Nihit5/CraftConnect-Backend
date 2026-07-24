package com.nihit.craft_connect.dto.khalti;

import lombok.*;

@Getter
@Setter
public class KhaltiInitiateRequest {
    private String return_url;
    private String website_url;
    private Long amount;              // paisa
    private String purchase_order_id;
    private String purchase_order_name;
    private KhaltiCustomerInfo customer_info;
}