package com.nihit.craft_connect.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ArtistProfileResponsePojo {
    private Long userId;
    private String firstName;
    private String lastName;
    private String displayPicture;

    private String artSpecialization;
    private String bio;
    private String province;
    private String district;
    private String address;
    private Double latitude;
    private Double longitude;
    private String coverImagePath;

    private List<ArtistWorkResponsePojo> works;
}
