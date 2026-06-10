package com.nihit.craft_connect.controller.user;

import com.nihit.craft_connect.constants.SuccessConstants;
import com.nihit.craft_connect.controller.BaseController;
import com.nihit.craft_connect.dto.GlobalApiResponse;
import com.nihit.craft_connect.enums.Status;
import com.nihit.craft_connect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/location")
@RequiredArgsConstructor
public class LocationController extends BaseController {
    private final UserRepository userRepository;
    @GetMapping("/province")
    public ResponseEntity<GlobalApiResponse> getProvince() {
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE),
                userRepository.fetchProvinces()));
    }
    @GetMapping("/district/{id}")
    public ResponseEntity<GlobalApiResponse> getDistrict(@PathVariable Long id) {
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE),
                userRepository.fetchDistricts(id)));
    }
}
