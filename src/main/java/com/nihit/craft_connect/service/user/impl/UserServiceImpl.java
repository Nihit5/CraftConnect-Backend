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
import com.nihit.craft_connect.entity.Cart;
import com.nihit.craft_connect.entity.User;
import com.nihit.craft_connect.entity.VendorDetails;
import com.nihit.craft_connect.enums.Status;
import com.nihit.craft_connect.exception.AppException;
import com.nihit.craft_connect.exception.InvalidCredentialsException;
import com.nihit.craft_connect.repository.CartRepository;
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
import java.util.*;

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
    private final CartRepository cartRepository;

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
            if ("ROLE_VENDOR".equals(userRequestPojo.getRole())) {
                user.setRole("ROLE_VENDOR");
                user.setStatus(Status.valueOf("PENDING"));
            } else if ("ROLE_ARTIST".equals(userRequestPojo.getRole())) {
                user.setRole("ROLE_ARTIST");
                user.setStatus(Status.valueOf("PENDING"));
            }
            else {
                user.setRole("ROLE_USER");
                user.setStatus(Status.valueOf("APPROVED"));
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
//        user.setStatus(Status.valueOf(userRequestPojo.getStatus()));

        user.setDisplayPicturePath(fileService.uploadAttachment(userRequestPojo.getDisplayPicture()));
        if ("ROLE_VENDOR".equals(userRequestPojo.getRole())) {

            VendorDetails vendorDetails = new VendorDetails();

            vendorDetails.setBusinessName(userRequestPojo.getBusinessName());

            vendorDetails.setProvince(userRequestPojo.getProvince());

            vendorDetails.setDistrict(userRequestPojo.getDistrict());

            vendorDetails.setAddress(userRequestPojo.getAddress());

            vendorDetails.setCitizenshipFrontImagePath(fileService.uploadAttachment(userRequestPojo.getCitizenshipFrontImage()));

            vendorDetails.setCitizenshipBackImagePath(fileService.uploadAttachment(userRequestPojo.getCitizenshipBackImage()));

            vendorDetails.setPancardPath(fileService.uploadAttachment(userRequestPojo.getPanCardImage()));

            user.setVendorDetails(vendorDetails);
        }
        userRepository.save(user);
        if (user.getRole().equals("ROLE_USER")){
            Cart cart = new Cart();
            cart.setUser(user);

            cartRepository.save(cart);
        }
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
        switch (user.getStatus()) {
            case PENDING:
                throw new AppException("Your account is pending approval. Please wait for an administrator to approve your account.");

            case REJECTED:
                throw new AppException("Your account has been rejected. Please contact the administrator for more information.");

            case SUSPENDED:
                throw new AppException("Your account has been suspended. Please contact the administrator for assistance.");

            case APPROVED:
                break;

            default:
                throw new AppException("Your account is not authorized to log in. Please contact the administrator.");
        }
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
    userDetailsPojo.setId(user.getId());
    userDetailsPojo.setFirstName(user.getFirstName());
    userDetailsPojo.setLastName(user.getLastName());
    userDetailsPojo.setEmail(user.getEmail());
    userDetailsPojo.setMobileNumber(user.getMobileNumber());
    userDetailsPojo.setDisplayPicture(extractFileName(user.getDisplayPicturePath()));
    userDetailsPojo.setRole(user.getRole());
    if (user.getRole().equals("ROLE_VENDOR")) {
        userDetailsPojo.setBusinessName(user.getVendorDetails().getBusinessName());
        userDetailsPojo.setProvince(user.getVendorDetails().getProvince());
        userDetailsPojo.setDistrict(user.getVendorDetails().getDistrict());
        userDetailsPojo.setAddress(user.getVendorDetails().getAddress());
        userDetailsPojo.setCitizenshipFrontImagePath(extractFileName(user.getVendorDetails().getCitizenshipFrontImagePath()));
        userDetailsPojo.setCitizenshipBackImagePath(extractFileName(user.getVendorDetails().getCitizenshipBackImagePath()));
        userDetailsPojo.setPancardPath(extractFileName(user.getVendorDetails().getPancardPath()));
        userDetailsPojo.setStatus(String.valueOf(user.getStatus()));
    }
    return userDetailsPojo;
    }
    public String extractFileName(String fullPath) {
        if (fullPath == null || fullPath.isBlank()) {
            return null;
        }
        return Paths.get(fullPath).getFileName().toString();
    }

    @Override
    public List<UserDetailsPojo> getAll(String role, Status status) {
        List<User> users;
        users = userRepository.findByRoleAndStatus(role, status);
        return users.stream()
                .map(this::map)
                .toList();
    }

    @Override
    public void updateStatus(Long id, Status status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        customMessageSource.get(
                                StringConstants.NOT_FOUND,
                                "USER"
                        )
                ));
        try {
            user.setStatus(status);
        } catch (IllegalArgumentException ex) {
            throw new AppException("Invalid status.");
        }
        userRepository.save(user);
    }

    private UserDetailsPojo map(User user) {

        UserDetailsPojo response = new UserDetailsPojo();

        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setMobileNumber(user.getMobileNumber());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus().name());
        response.setDisplayPicture(
                fileService.extractFileName(user.getDisplayPicturePath())
        );

        if (user.getVendorDetails() != null) {

            response.setBusinessName(user.getVendorDetails().getBusinessName());
            response.setProvince(user.getVendorDetails().getProvince());
            response.setDistrict(user.getVendorDetails().getDistrict());
            response.setAddress(user.getVendorDetails().getAddress());

            response.setCitizenshipFrontImagePath(
                    fileService.extractFileName(user.getVendorDetails().getCitizenshipFrontImagePath())
            );

            response.setCitizenshipBackImagePath(
                    fileService.extractFileName(user.getVendorDetails().getCitizenshipBackImagePath())
            );

            response.setPancardPath(
                    fileService.extractFileName(user.getVendorDetails().getPancardPath())
            );


        }

        return response;
    }
}
