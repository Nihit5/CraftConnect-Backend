package com.nihit.craft_connect.service.artist;

import com.nihit.craft_connect.config.UserDetailConfig;
import com.nihit.craft_connect.dto.user.ArtistListItemPojo;
import com.nihit.craft_connect.dto.user.ArtistProfileResponsePojo;
import com.nihit.craft_connect.dto.user.ArtistWorkRequestPojo;
import com.nihit.craft_connect.dto.user.ArtistWorkResponsePojo;
import com.nihit.craft_connect.entity.ArtistDetails;
import com.nihit.craft_connect.entity.ArtistWork;
import com.nihit.craft_connect.entity.User;
import com.nihit.craft_connect.enums.Status;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.repository.ArtistWorkRepository;
import com.nihit.craft_connect.repository.UserRepository;
import com.nihit.craft_connect.service.file.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistProfileServiceImpl implements ArtistProfileService {

    private final UserRepository userRepository;
    private final ArtistWorkRepository artistWorkRepository;
    private final UserDetailConfig userDetailConfig;
    private final FileService fileService;

    @Override
    @Transactional(readOnly = true)
    public ArtistProfileResponsePojo getMyProfile() {
        Long userId = userDetailConfig.getLoggedInUserId();
        return buildProfileResponse(getArtistUserOrThrow(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public ArtistProfileResponsePojo getPublicProfile(Long artistId) {
        User artist = getArtistUserOrThrow(artistId);
        return buildProfileResponse(artist);
    }

    @Override
    @Transactional
    public ArtistWorkResponsePojo addWork(ArtistWorkRequestPojo request) {
        Long userId = userDetailConfig.getLoggedInUserId();
        User artist = getArtistUserOrThrow(userId);

        if (request.getImage() == null || request.getImage().isEmpty()) {
            throw new AppException("Please upload an image for this work.");
        }

        ArtistWork work = new ArtistWork();
        work.setArtist(artist);
        work.setTitle(request.getTitle());
        work.setDescription(request.getDescription());
        work.setImagePath(fileService.uploadAttachment(request.getImage()));
        work.setDisplayOrder(request.getDisplayOrder());
        work.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        work.setModifiedDate(new Timestamp(System.currentTimeMillis()));

        artistWorkRepository.save(work);
        return mapWorkToResponse(work);
    }

    @Override
    @Transactional
    public ArtistWorkResponsePojo updateWork(Long workId, ArtistWorkRequestPojo request) {
        Long userId = userDetailConfig.getLoggedInUserId();
        ArtistWork work = artistWorkRepository.findByIdAndArtist_Id(workId, userId)
                .orElseThrow(() -> new AppException("Work not found or does not belong to you."));

        if (request.getTitle() != null) {
            work.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            work.setDescription(request.getDescription());
        }
        if (request.getDisplayOrder() != null) {
            work.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            work.setImagePath(fileService.uploadAttachment(request.getImage()));
        }
        work.setModifiedDate(new Timestamp(System.currentTimeMillis()));

        artistWorkRepository.save(work);
        return mapWorkToResponse(work);
    }

    @Override
    @Transactional
    public void deleteWork(Long workId) {
        Long userId = userDetailConfig.getLoggedInUserId();
        ArtistWork work = artistWorkRepository.findByIdAndArtist_Id(workId, userId)
                .orElseThrow(() -> new AppException("Work not found or does not belong to you."));
        artistWorkRepository.delete(work);
    }

    private User getArtistUserOrThrow(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found."));
        if (!"ROLE_ARTIST".equals(user.getRole())) {
            throw new AppException("User is not an artist.");
        }
        if (user.getArtistDetails() == null) {
            throw new AppException("Artist details not found for this user.");
        }
        return user;
    }

    private ArtistProfileResponsePojo buildProfileResponse(User artist) {
        ArtistDetails details = artist.getArtistDetails();
        List<ArtistWork> works = artistWorkRepository
                .findByArtist_IdOrderByDisplayOrderAscCreatedDateDesc(artist.getId());

        ArtistProfileResponsePojo response = new ArtistProfileResponsePojo();
        response.setUserId(artist.getId());
        response.setFirstName(artist.getFirstName());
        response.setLastName(artist.getLastName());
        response.setDisplayPicture(fileService.extractFileName(artist.getDisplayPicturePath()));

        response.setArtSpecialization(details.getArtSpecialization());
        response.setBio(details.getBio());
        response.setProvince(details.getProvince());
        response.setDistrict(details.getDistrict());
        response.setAddress(details.getAddress());
        response.setLatitude(details.getLatitude());
        response.setLongitude(details.getLongitude());
        response.setCoverImagePath(fileService.extractFileName(details.getPortfolioImagePath()));

        response.setWorks(works.stream().map(this::mapWorkToResponse).toList());

        return response;
    }

    private ArtistWorkResponsePojo mapWorkToResponse(ArtistWork work) {
        ArtistWorkResponsePojo pojo = new ArtistWorkResponsePojo();
        pojo.setId(work.getId());
        pojo.setTitle(work.getTitle());
        pojo.setDescription(work.getDescription());
        pojo.setImagePath(fileService.extractFileName(work.getImagePath()));
        pojo.setDisplayOrder(work.getDisplayOrder());
        pojo.setCreatedDate(work.getCreatedDate());
        return pojo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArtistListItemPojo> getPublicArtistList() {
        List<User> artists = userRepository.findByRoleAndStatus("ROLE_ARTIST", Status.APPROVED);

        return artists.stream()
                .filter(a -> a.getArtistDetails() != null)
                .map(this::mapToListItem)
                .toList();
    }

    private ArtistListItemPojo mapToListItem(User artist) {
        ArtistDetails details = artist.getArtistDetails();

        ArtistListItemPojo pojo = new ArtistListItemPojo();
        pojo.setId(artist.getId());
        pojo.setFirstName(artist.getFirstName());
        pojo.setLastName(artist.getLastName());
        pojo.setDisplayPicture(fileService.extractFileName(artist.getDisplayPicturePath()));
        pojo.setArtSpecialization(details.getArtSpecialization());
        pojo.setBio(details.getBio());
        pojo.setProvince(details.getProvince());
        pojo.setDistrict(details.getDistrict());
        pojo.setLatitude(details.getLatitude());
        pojo.setLongitude(details.getLongitude());
        pojo.setCoverImagePath(fileService.extractFileName(details.getPortfolioImagePath()));
        return pojo;
    }
}
