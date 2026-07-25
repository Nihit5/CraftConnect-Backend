package com.nihit.craft_connect.dto.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ArtistWorkRequestPojo {
    private Long id;
    private String title;
    private String description;
    private Integer displayOrder;
    private MultipartFile image;
}
