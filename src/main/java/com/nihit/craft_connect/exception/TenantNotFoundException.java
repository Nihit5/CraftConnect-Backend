package com.nihit.craft_connect.exception;

/**
 * @author bkings
 * @version 1.0.0
 * @since 2023-08-26 14:20
 */

public class TenantNotFoundException extends RuntimeException {
    public TenantNotFoundException(String message) {
        super(message);
    }
}
