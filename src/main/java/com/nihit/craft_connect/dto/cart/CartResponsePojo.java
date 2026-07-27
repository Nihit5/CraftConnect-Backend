package com.nihit.craft_connect.dto.cart;

import lombok.*;

import java.util.List;

@Getter
@Setter
public class CartResponsePojo {
    private Long cartId;
    private Long productCount;
    private Integer totalItems;
    private Double totalPrice;
    private List<VendorCartGroupPojo> vendorGroups;
}