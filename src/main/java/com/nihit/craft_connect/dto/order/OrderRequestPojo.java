package com.nihit.craft_connect.dto.order;

import com.nihit.craft_connect.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequestPojo {
    private String shippingAddress;
    private PaymentMethod paymentMethod;
    private List<Long> cartProductIds;
}
