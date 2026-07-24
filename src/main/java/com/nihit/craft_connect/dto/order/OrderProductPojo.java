package com.nihit.craft_connect.dto.order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderProductPojo {
    private Long productId;
    private String name;
    private String imagePath;
    private Integer quantity;
    private Double priceAtPurchase;
    private Double subTotal;
}
