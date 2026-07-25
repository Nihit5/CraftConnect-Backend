package com.nihit.craft_connect.enums;

public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUND_PENDING,   // vendor cancelled after payment succeeded — needs manual refund via merchant panel
    REFUNDED
}
