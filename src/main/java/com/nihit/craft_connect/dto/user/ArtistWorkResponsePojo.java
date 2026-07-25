package com.nihit.craft_connect.dto.user;

import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Getter
@Setter
public class ArtistWorkResponsePojo {
    private Long id;
    private String title;
    private String description;
    private String imagePath;
    private Integer displayOrder;
    private Timestamp createdDate;
}
