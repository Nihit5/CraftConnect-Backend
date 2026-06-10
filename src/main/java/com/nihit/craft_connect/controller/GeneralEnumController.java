package com.nihit.craft_connect.controller;

import com.nihit.craft_connect.constants.SuccessConstants;
import com.nihit.craft_connect.dto.GlobalApiResponse;
import com.nihit.craft_connect.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/enum")
@RequiredArgsConstructor
public class GeneralEnumController extends BaseController {
    @GetMapping("/status")
    public ResponseEntity<GlobalApiResponse> getActionList() {
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_RETRIEVE),
                Status.getStatusList()));
    }
}
