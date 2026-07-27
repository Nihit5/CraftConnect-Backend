package com.nihit.craft_connect.dto.cart;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class VendorCartGroupPojo {
    private Long vendorId;
    private String vendorName;
    private List<CartProductPojo> products;
    private Integer itemCount;
    private Double subTotal;
}
