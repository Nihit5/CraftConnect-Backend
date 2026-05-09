package com.nihit.craft_connect.controller.user;

import com.nihit.craft_connect.constants.SuccessConstants;
import com.nihit.craft_connect.controller.BaseController;
import com.nihit.craft_connect.dto.GlobalApiResponse;
import com.nihit.craft_connect.dto.user.UserRequestPojo;
import com.nihit.craft_connect.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController extends BaseController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<GlobalApiResponse> save(@Valid @ModelAttribute UserRequestPojo userRequestPojo) {
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_CREATE),
                userService.saveOrUpdate(userRequestPojo)));
    }

    @PutMapping("/update")
    public ResponseEntity<GlobalApiResponse> update(@Valid @ModelAttribute UserRequestPojo userRequestPojo) {
        return ResponseEntity.ok(successResponse(customMessageSource.get(SuccessConstants.SUCCESS_CREATE),
                userService.saveOrUpdate(userRequestPojo)));
    }
}
