package com.nihit.craft_connect.dto.exception;


import com.nihit.craft_connect.enums.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GlobalExceptionResponse {
    private ResponseStatus status;
    private int httpCode;
    private String message;
    private List<String> errors;

    public GlobalExceptionResponse(ResponseStatus status, int httpCode, String message, String error) {
        this.status = status;
        this.httpCode = httpCode;
        this.message = message;
        this.errors = Arrays.asList(error);
    }
}



