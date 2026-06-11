package com.nihit.craft_connect.dto.user;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsPojo {
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String role;
    private String businessName;
    private String province;
    private String district;
    private String address;
    private String citizenshipFrontImagePath;
    private String citizenshipBackImagePath;
    private String pancardPath;
}
