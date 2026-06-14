package com.nihit.craft_connect.dto.category;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nihit.craft_connect.constants.FieldErrorConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private Long id;
    @NotNull(message = FieldErrorConstant.NOT_NULL)
    @NotBlank(message = FieldErrorConstant.NOT_BLANK)
    private String name;
    @JsonIgnore
    private MultipartFile image;
    private String imagePath;
}
