package com.nihit.craft_connect.dto.order;

import com.nihit.craft_connect.enums.PaymentMethod;
import com.nihit.craft_connect.enums.PaymentStatus;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
public class VendorPaymentDetailPojo {
    private Long orderId;
    private Long orderProductId;

    private Long productId;
    private String productName;
    private Integer quantity;
    private Double itemSubTotal;
    private Double productPrice;

    private String customerName;
    private String mobileNumber;

    private Double orderTotalAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private Double paymentAmount;
    private Timestamp paymentDate;

    private Double refundAmount;
    private String refundNotes;
}
