package com.nihit.craft_connect.dto.user;

import com.nihit.craft_connect.constants.FieldErrorConstant;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestPojo {
    private Long id;
    @NotNull(message = FieldErrorConstant.NOT_NULL)
    @NotBlank(message = FieldErrorConstant.NOT_BLANK)
    private String firstName;
    @NotNull(message = FieldErrorConstant.NOT_NULL)
    @NotBlank(message = FieldErrorConstant.NOT_BLANK)
    private String lastName;
    @NotNull(message = FieldErrorConstant.NOT_NULL)
    @NotBlank(message = FieldErrorConstant.NOT_BLANK)
    private String email;
    @NotNull(message = FieldErrorConstant.NOT_NULL)
    @NotBlank(message = FieldErrorConstant.NOT_BLANK)
    @Pattern(regexp = "^[0-9]{10}$", message = FieldErrorConstant.MOBILE_LENGTH)
    private String mobileNumber;
    @NotNull(message = FieldErrorConstant.NOT_NULL)
    @NotBlank(message = FieldErrorConstant.NOT_BLANK)
    @Size(min = 8,  message = FieldErrorConstant.PASSWORD_MIN_LENGTH)
    private String password;
    @NotNull(message = FieldErrorConstant.NOT_NULL)
    @NotBlank(message = FieldErrorConstant.NOT_BLANK)
    private String confirmPassword;
    @NotNull(message = FieldErrorConstant.NOT_NULL)
    @NotBlank(message = FieldErrorConstant.NOT_BLANK)
    private String role;
    private MultipartFile displayPicture;
    private String businessName;
    private String province;
    private String district;
    private String address;

    private MultipartFile citizenshipFrontImage;
    private MultipartFile citizenshipBackImage;
    private MultipartFile panCardImage;
    private String status;

    private String artSpecialization;
    private String bio;
    private String artistProvince;
    private String artistDistrict;
    private String artistAddress;
    private Double latitude;
    private Double longitude;
    private MultipartFile artistCitizenshipFrontImage;
    private MultipartFile artistCitizenshipBackImage;
    private MultipartFile portfolioImage;
}
