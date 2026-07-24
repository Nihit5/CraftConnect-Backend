package com.nihit.craft_connect.dto.esewa;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "esewa")
public class EsewaConfig {
    private String productCode;     // EPAYTEST for sandbox
    private String secretKey;       // 8gBm/:&EnhH.1/q for sandbox (test key, public in eSewa docs)
    private String formUrl;         // https://rc-epay.esewa.com.np/api/epay/main/v2/form (sandbox)
    private String statusCheckUrl;  // https://rc.esewa.com.np/api/epay/transaction/status/ (sandbox)
    private String successUrl;      // your backend callback for success
    private String failureUrl;      // your backend callback for failure
}