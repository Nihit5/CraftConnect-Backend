package com.nihit.craft_connect.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestPojo {
    private Long id;

    private String name;

    private String description;

    private MultipartFile image;

    private Double price;

    private Long quantity;

    private Boolean featured;

    private Long categoryId;
}
