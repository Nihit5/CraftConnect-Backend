package com.nihit.craft_connect.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartProductPojo {

    private Long productId;

    private String name;

    private String description;

    private String imagePath;

    private Double price;

    private Long availableQuantity;

    private Integer cartQuantity;

    private Double subTotal;
    private Long cartProductId;
}
