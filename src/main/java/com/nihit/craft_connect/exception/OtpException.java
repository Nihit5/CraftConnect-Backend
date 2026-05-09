package com.nihit.craft_connect.exception;

import lombok.Getter;

/**
 * @CreatedBy Suprit Darshan Shrestha
 * @CreatedOn 2024-05-02 10:17
 */
@Getter
public class OtpException extends RuntimeException {

    private String issuer;
    private String secret;
    private String uuid;
    private boolean totpEnabled;

    public OtpException(String issuer, String secret, String uuid, boolean totpEnabled) {
        this.issuer = issuer;
        this.secret = secret;
        this.uuid = uuid;
        this.totpEnabled = totpEnabled;
    }
}
