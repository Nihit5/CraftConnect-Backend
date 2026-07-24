package com.nihit.craft_connect.dto.khalti;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "khalti")
public class KhaltiConfig {
    private String secretKey;          // e.g. live_secret_key_xxxx (sandbox key from test-admin.khalti.com)
    private String baseUrl;            // https://dev.khalti.com/api/v2 (sandbox) or https://khalti.com/api/v2 (prod)
    private String returnUrl;          // your frontend/backend URL that Khalti redirects back to
    private String websiteUrl;         // your site's public URL
}
