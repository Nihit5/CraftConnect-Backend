package com.nihit.craft_connect.dto.khalti;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KhaltiInitiateResponse {
    private String pidx;
    private String payment_url;
    private String expires_at;
    private Integer expires_in;

    // populated on error instead
    private String detail;
    private String error_key;
}
