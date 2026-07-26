package com.nihit.craft_connect.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArtistListItemPojo {
    private Long id;
    private String firstName;
    private String lastName;
    private String displayPicture;
    private String artSpecialization;
    private String bio;
    private String province;
    private String district;
    private Double latitude;
    private Double longitude;
    private String coverImagePath;
}
