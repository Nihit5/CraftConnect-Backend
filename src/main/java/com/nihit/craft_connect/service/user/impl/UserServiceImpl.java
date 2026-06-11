package com.nihit.craft_connect.service.user.impl;

import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.config.JwtTokenHelper;
import com.nihit.craft_connect.config.UserDetailConfig;
import com.nihit.craft_connect.constants.ErrorConstants;
import com.nihit.craft_connect.constants.MessageConstant;
import com.nihit.craft_connect.constants.StringConstants;
import com.nihit.craft_connect.dto.login.LoginRequest;
import com.nihit.craft_connect.dto.login.LoginResponse;
import com.nihit.craft_connect.dto.user.UserDetailsPojo;
import com.nihit.craft_connect.dto.user.UserRequestPojo;
import com.nihit.craft_connect.dto.user.UserResponsePojo;
import com.nihit.craft_connect.entity.User;
import com.nihit.craft_connect.entity.VendorDetails;
import com.nihit.craft_connect.enums.Status;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.exception.InvalidCredentialsException;
import com.nihit.craft_connect.repository.UserRepository;
import com.nihit.craft_connect.service.file.FileService;
import com.nihit.craft_connect.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CustomMessageSource customMessageSource;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final JwtTokenHelper jwtTokenHelper;
    private final AuthenticationManager authenticationManager;
    private static final String FILE_LOCATION = "users";
    private final UserDetailConfig userDetailConfig;

    @Override
    public UserResponsePojo saveOrUpdate(UserRequestPojo userRequestPojo) {
        User user;
        if (userRequestPojo.getId() != null) {
            user = userRepository.findById(userRequestPojo.getId()).orElseThrow(() -> new AppException(
                    customMessageSource.get(ErrorConstants.ERROR_ALREADY_EXIST,
                            customMessageSource.get(StringConstants.USER))
            ));
        }
        else {
            user = new User();
            if (userRepository.existsByEmail(userRequestPojo.getEmail())) {
                throw new AppException("Email already exists");
            }
            if (userRepository.existsByMobileNumber(userRequestPojo.getMobileNumber())) {
                throw new AppException("Mobile number already exists");
            }
        }
        if (!Objects.equals(userRequestPojo.getPassword(), userRequestPojo.getConfirmPassword())) {
            throw new AppException(customMessageSource.get(StringConstants.INVALID_PASSWORD));
        }
        user.setFirstName(userRequestPojo.getFirstName());
        user.setLastName(userRequestPojo.getLastName());
        user.setEmail(userRequestPojo.getEmail());
        user.setPassword(passwordEncoder.encode(userRequestPojo.getPassword()));
        user.setMobileNumber(userRequestPojo.getMobileNumber());
        if ("ROLE_VENDOR".equals(userRequestPojo.getRole())) {
            user.setRole("ROLE_VENDOR");
        } else if ("ROLE_ARTIST".equals(userRequestPojo.getRole())) {
            user.setRole("ROLE_ARTIST");
        }
        else {
            user.setRole("ROLE_USER");
        }
        user.setDisplayPicturePath(fileService.uploadAttachment(userRequestPojo.getDisplayPicture(), FILE_LOCATION));
        if ("ROLE_VENDOR".equals(userRequestPojo.getRole())) {

            VendorDetails vendorDetails = new VendorDetails();

            vendorDetails.setBusinessName(userRequestPojo.getBusinessName());

            vendorDetails.setProvince(userRequestPojo.getProvince());

            vendorDetails.setDistrict(userRequestPojo.getDistrict());

            vendorDetails.setAddress(userRequestPojo.getAddress());

            vendorDetails.setCitizenshipFrontImagePath(fileService.uploadAttachment(userRequestPojo.getCitizenshipFrontImage(), FILE_LOCATION));

            vendorDetails.setCitizenshipBackImagePath(fileService.uploadAttachment(userRequestPojo.getCitizenshipBackImage(), FILE_LOCATION));

            vendorDetails.setPancardPath(fileService.uploadAttachment(userRequestPojo.getPanCardImage(), FILE_LOCATION));

            vendorDetails.setStatus(Status.PENDING);

            user.setVendorDetails(vendorDetails);
        }
        userRepository.save(user);
        UserResponsePojo userResponsePojo = new UserResponsePojo();
        userResponsePojo.setId(user.getId());
        userResponsePojo.setFirstName(user.getFirstName());
        userResponsePojo.setLastName(user.getLastName());
        userResponsePojo.setEmail(user.getEmail());
        userResponsePojo.setMobileNumber(user.getMobileNumber());
        userResponsePojo.setRole(user.getRole());
        return userResponsePojo;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() ->
                        new AppException("User not found")
                );

        try {

            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            String token = jwtTokenHelper.generateToken(user.getId(), user.getEmail(), "user");

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setTokenExpiryDate(
                    jwtTokenHelper.getExpirationDateFromToken(token)
            );
            response.setRole(user.getRole());

            return response;

        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        } catch (AuthenticationException e) {
            throw new AppException("Authentication failed");
        }
    }

    @Override
    public UserDetailsPojo getUserDetails(){
    User user = userRepository.findById(userDetailConfig.getLoggedInUserId())
            .orElseThrow(() -> new AppException("User not found"));
    UserDetailsPojo userDetailsPojo = new UserDetailsPojo();
    userDetailsPojo.setFirstName(user.getFirstName());
    userDetailsPojo.setLastName(user.getLastName());
    userDetailsPojo.setEmail(user.getEmail());
    userDetailsPojo.setMobileNumber(user.getMobileNumber());
    userDetailsPojo.setRole(user.getRole());
    if (user.getRole().equals("ROLE_VENDOR")) {
        userDetailsPojo.setBusinessName(user.getVendorDetails().getBusinessName());
        userDetailsPojo.setProvince(user.getVendorDetails().getProvince());
        userDetailsPojo.setDistrict(user.getVendorDetails().getDistrict());
        userDetailsPojo.setAddress(user.getVendorDetails().getAddress());
        userDetailsPojo.setCitizenshipFrontImagePath(extractFileName(user.getVendorDetails().getCitizenshipFrontImagePath()));
        userDetailsPojo.setCitizenshipBackImagePath(extractFileName(user.getVendorDetails().getCitizenshipBackImagePath()));
        userDetailsPojo.setPancardPath(extractFileName(user.getVendorDetails().getPancardPath()));
    }
    return userDetailsPojo;
    }
    public String extractFileName(String fullPath) {
        if (fullPath == null || fullPath.isBlank()) {
            return null;
        }
        return Paths.get(fullPath).getFileName().toString();
    }
}
