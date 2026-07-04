package com.nihit.craft_connect.service.user;

import com.nihit.craft_connect.dto.login.LoginRequest;
import com.nihit.craft_connect.dto.login.LoginResponse;
import com.nihit.craft_connect.dto.user.UserDetailsPojo;
import com.nihit.craft_connect.dto.user.UserRequestPojo;
import com.nihit.craft_connect.dto.user.UserResponsePojo;
import com.nihit.craft_connect.enums.Status;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    UserResponsePojo saveOrUpdate(UserRequestPojo userRequestPojo);
    LoginResponse login(LoginRequest loginRequest);
    UserDetailsPojo getUserDetails();
    List<UserDetailsPojo> getAll(String role, Status status);
}
