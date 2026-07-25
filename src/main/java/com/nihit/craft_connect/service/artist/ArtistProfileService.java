package com.nihit.craft_connect.service.artist;

import com.nihit.craft_connect.dto.user.ArtistProfileResponsePojo;
import com.nihit.craft_connect.dto.user.ArtistWorkRequestPojo;
import com.nihit.craft_connect.dto.user.ArtistWorkResponsePojo;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface ArtistProfileService {
    ArtistProfileResponsePojo getMyProfile();
    ArtistProfileResponsePojo getPublicProfile(Long artistId);

    ArtistWorkResponsePojo addWork(ArtistWorkRequestPojo request);
    ArtistWorkResponsePojo updateWork(Long workId, ArtistWorkRequestPojo request);
    void deleteWork(Long workId);
}
