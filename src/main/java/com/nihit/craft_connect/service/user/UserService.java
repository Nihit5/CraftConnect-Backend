package com.nihit.craft_connect.service.user;

import com.nihit.craft_connect.dto.login.LoginRequest;
import com.nihit.craft_connect.dto.login.LoginResponse;
import com.nihit.craft_connect.dto.user.UserRequestPojo;
import com.nihit.craft_connect.dto.user.UserResponsePojo;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    UserResponsePojo saveOrUpdate(UserRequestPojo userRequestPojo);
    LoginResponse login(LoginRequest loginRequest);
}
