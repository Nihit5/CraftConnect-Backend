package com.nihit.craft_connect.dto.order;

import com.nihit.craft_connect.enums.OrderStatus;
import lombok.*;

@Getter
@Setter
public class VendorItemStatusUpdatePojo {
    private OrderStatus status;
    private String cancellationReason;   // optional — recorded into refundNotes when cancelling a paid item
}