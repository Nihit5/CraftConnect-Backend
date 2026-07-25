package com.nihit.craft_connect.controller.artist;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.constants.SuccessConstants;
import com.nihit.craft_connect.controller.BaseController;
import com.nihit.craft_connect.dto.GlobalApiResponse;
import com.nihit.craft_connect.dto.user.ArtistWorkRequestPojo;
import com.nihit.craft_connect.service.artist.ArtistProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/artist")
@RequiredArgsConstructor
public class ArtistProfileController extends BaseController {

    private final ArtistProfileService artistProfileService;
    private final CustomMessageSource customMessageSource;

    @GetMapping("/my-profile")
    public ResponseEntity<GlobalApiResponse> getMyProfile() {
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Profile"),
                artistProfileService.getMyProfile()));
    }

    // public — anyone browsing the marketplace can view an artist's profile page
    @GetMapping("/{artistId}")
    public ResponseEntity<GlobalApiResponse> getPublicProfile(@PathVariable Long artistId) {
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE, "Profile"),
                artistProfileService.getPublicProfile(artistId)));
    }

    @PostMapping("/work/add")
    public ResponseEntity<GlobalApiResponse> addWork(@ModelAttribute ArtistWorkRequestPojo request) {
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_CREATE, "Work"),
                artistProfileService.addWork(request)));
    }

    @PutMapping("/work/{workId}")
    public ResponseEntity<GlobalApiResponse> updateWork(
            @PathVariable Long workId, @ModelAttribute ArtistWorkRequestPojo request) {
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_UPDATE, "Work"),
                artistProfileService.updateWork(workId, request)));
    }

    @DeleteMapping("/work/{workId}")
    public ResponseEntity<GlobalApiResponse> deleteWork(@PathVariable Long workId) {
        artistProfileService.deleteWork(workId);
        return ResponseEntity.ok(successResponse(
                customMessageSource.get(SuccessConstants.SUCCESS_DELETE, "Work"), null));
    }
}
