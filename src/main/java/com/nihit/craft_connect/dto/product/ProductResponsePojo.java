package com.nihit.craft_connect.dto.product;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponsePojo {

    private Long id;

    private String name;

    private String description;

    private String imagePath;

    private Double price;

    private Long quantity;

    private Boolean featured = false;

    private Long userId;

    private String userName;

    private Long categoryId;

    private String categoryName;
}
