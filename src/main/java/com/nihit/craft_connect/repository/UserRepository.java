package com.nihit.craft_connect.repository;

import com.nihit.craft_connect.dto.location.DistrictDto;
import com.nihit.craft_connect.dto.location.ProvinceDto;
import com.nihit.craft_connect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByMobileNumber(String mobileNumber);
    @Query(value = "select * from districts where province_id = ?1 order by district_id", nativeQuery = true)
    List<DistrictDto> fetchDistricts(Long provinceId);
    @Query(value = "select * from provinces order by province_id", nativeQuery = true)
    List<ProvinceDto> fetchProvinces();
}
