package com.nihit.craft_connect.dto.order;

import com.nihit.craft_connect.enums.OrderStatus;
import com.nihit.craft_connect.enums.PaymentMethod;
import com.nihit.craft_connect.enums.PaymentStatus;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
public class VendorOrderItemPojo {
    private Long orderProductId;
    private Long orderId;
    private String orderUuid;
    private Timestamp orderedDate;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private Double priceAtPurchase;
    private Double subTotal;
    private OrderStatus itemStatus;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String recipientName;
    private String mobileNumber;
    private String province;
    private String district;
    private String shippingAddressLine;
    private String landmark;
}
