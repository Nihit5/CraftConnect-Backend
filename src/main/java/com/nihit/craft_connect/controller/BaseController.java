package com.nihit.craft_connect.controller;


import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.constants.SuccessConstants;
import com.nihit.craft_connect.dto.GlobalApiResponse;
import com.nihit.craft_connect.enums.ResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;


public class BaseController {
    protected static final ResponseStatus API_SUCCESS_STATUS = ResponseStatus.SUCCESS;
    protected static final ResponseStatus API_FAILURE_STATUS = ResponseStatus.FAILURE;
    @Autowired
    protected CustomMessageSource customMessageSource;

    protected String permissionName = this.getClass().getSimpleName();
    protected String moduleName;

    protected GlobalApiResponse successResponse(String message, Object data) {
        GlobalApiResponse response = new GlobalApiResponse();
        response.setStatus(API_SUCCESS_STATUS);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    protected GlobalApiResponse successResponse(String message) {
        GlobalApiResponse response = new GlobalApiResponse();
        response.setStatus(API_SUCCESS_STATUS);
        response.setMessage(message);
        return response;
    }

    protected GlobalApiResponse successCreate(String moduleName, Object data) {
        GlobalApiResponse response = new GlobalApiResponse();
        String message = customMessageSource.get(SuccessConstants.SUCCESS_CREATE, customMessageSource.get(moduleName));
        response.setStatus(API_SUCCESS_STATUS);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    protected GlobalApiResponse failureResponse(String message, Object data) {
        GlobalApiResponse apiResponse = new GlobalApiResponse();
        apiResponse.setMessage(message);
        apiResponse.setData(data);
        apiResponse.setStatus(ResponseStatus.FAILURE);
        return apiResponse;
    }
}
