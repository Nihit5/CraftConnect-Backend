package com.nihit.craft_connect.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArtistDetailsResponsePojo {
    private String artSpecialization;
    private String bio;
    private String province;
    private String district;
    private String address;
    private Double latitude;
    private Double longitude;
    private String portfolioImagePath;
}
