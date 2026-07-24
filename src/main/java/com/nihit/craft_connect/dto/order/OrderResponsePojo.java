package com.nihit.craft_connect.dto.order;

import com.nihit.craft_connect.enums.OrderStatus;
import com.nihit.craft_connect.enums.PaymentMethod;
import com.nihit.craft_connect.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OrderResponsePojo {
    private Long orderId;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private Double totalAmount;
    private String shippingAddress;
    private List<OrderProductPojo> products;

    // populated only when paymentMethod is ESEWA/KHALTI — frontend redirects here
    private String paymentRedirectUrl;
    private java.util.Map<String, String> paymentFormFields; // esewa needs signed form fields
}
