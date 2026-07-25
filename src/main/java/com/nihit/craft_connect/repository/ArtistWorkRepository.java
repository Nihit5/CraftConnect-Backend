package com.nihit.craft_connect.repository;

import com.nihit.craft_connect.entity.ArtistWork;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ArtistWorkRepository extends JpaRepository<ArtistWork, Long> {
    List<ArtistWork> findByArtist_IdOrderByDisplayOrderAscCreatedDateDesc(Long artistId);
    Optional<ArtistWork> findByIdAndArtist_Id(Long id, Long artistId);
}
