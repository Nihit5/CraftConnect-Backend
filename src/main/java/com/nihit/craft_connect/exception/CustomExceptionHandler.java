package com.nihit.craft_connect.exception;


import com.nihit.craft_connect.config.CustomMessageSource;
import com.nihit.craft_connect.constants.ErrorCodeConstants;
import com.nihit.craft_connect.constants.FieldErrorConstant;
import com.nihit.craft_connect.dto.exception.GlobalExceptionResponse;
import com.nihit.craft_connect.enums.ResponseStatus;
import com.nihit.craft_connect.utils.Capital;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
    private final CustomMessageSource customMessageSource;

    public CustomExceptionHandler(CustomMessageSource customMessageSource) {
        this.customMessageSource = customMessageSource;
    }

    private GlobalExceptionResponse genericWithMessage(HttpStatus httpStatus, String message, String error) {
        return new GlobalExceptionResponse(ResponseStatus.FAILURE, httpStatus.value(), message, error);
    }

    private GlobalExceptionResponse genericWithMessage(HttpStatus httpStatusCode, String message, List<String> errors) {
        return new GlobalExceptionResponse(ResponseStatus.FAILURE, httpStatusCode.value(), message, errors);
    }

    private GlobalExceptionResponse genericWithMessage(int httpStatusCode, String message, String error) {
        return new GlobalExceptionResponse(ResponseStatus.FAILURE, httpStatusCode, message, error);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logger.error(ex.getClass().getName());
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        final GlobalExceptionResponse apiError = new GlobalExceptionResponse(ResponseStatus.FAILURE, httpStatus.value(), ex.getMessage(), ex.getMessage());
        return handleExceptionInternal(ex, apiError, headers, httpStatus, request);
    }

    @Override
    @ResponseBody
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatusCode status, final WebRequest request) {
        final List<String> errors = new ArrayList<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            validation(errors, error);
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        return handleExceptionInternal(ex, genericWithMessage(httpStatus, errors.get(0), errors), headers, httpStatus, request);

    }

    private void validation(List<String> errors, FieldError error) {
        if (error != null) {
            // Validation name
            String errorCode = error.getCode();
            String errorField = error.getField().toLowerCase();
            String defaultMessage = error.getDefaultMessage();
            Object rejectedValue = error.getRejectedValue();
            defaultMessage = (defaultMessage != null && !defaultMessage.equalsIgnoreCase("")) ? defaultMessage : errorField + " field validation error.";
            Object[] errorArguments = error.getArguments();
            String errorFieldValidationCode = "";
            try {
                errorFieldValidationCode = customMessageSource.get(errorField);
            } catch (Exception e) {
                try {
                    errorFieldValidationCode = customMessageSource.get(errorField.toUpperCase());
                } catch (Exception ae) {
                    errorFieldValidationCode = errorField;
                }
            }
            validationTypes(errorCode, defaultMessage, errorFieldValidationCode, errorArguments, rejectedValue, errors);
        }
    }

    private void validationTypes(String errorCode, String defaultMessage, String errorField, Object[] errorArguments, Object rejectedValue, List<String> errors) {
        String errorFieldValidationCode;
        try {
            errorFieldValidationCode = customMessageSource.get(errorField);
        } catch (Exception e) {
            errorFieldValidationCode = errorField;
        }
        try {
            switch (errorCode) {
                case ErrorCodeConstants.NOT_NULL:
                    errors.add(customMessageSource.get(FieldErrorConstant.NOT_NULL, errorFieldValidationCode));
                    break;
                case ErrorCodeConstants.NOT_BLANK:
                    errors.add(customMessageSource.get(FieldErrorConstant.NOT_BLANK, errorFieldValidationCode));
                    break;
                case ErrorCodeConstants.NOT_EMPTY:
                    errors.add(customMessageSource.get(FieldErrorConstant.NOT_EMPTY, errorFieldValidationCode));
                    break;
                case ErrorCodeConstants.SIZE:
                    if ("mobileNumber".equalsIgnoreCase(errorField)) {
                        errors.add(customMessageSource.get(FieldErrorConstant.MOBILE_LENGTH));
                    } else {
                        errorArguments = errorArguments == null ? new Object[]{"", ""} : errorArguments;
                        errors.add(customMessageSource.get(FieldErrorConstant.SIZE, errorFieldValidationCode, errorArguments[2], errorArguments[1]));
                    }
                    break;
                case ErrorCodeConstants.EMAIL:
                    errors.add(customMessageSource.get(FieldErrorConstant.EMAIL, rejectedValue));
                    break;
                case ErrorCodeConstants.LENGTH:
                    errors.add(customMessageSource.get(FieldErrorConstant.LENGTH, errorArguments[1], errorArguments[2]));
                    break;
                case ErrorCodeConstants.PATTERN:
                    errors.add(customMessageSource.get(FieldErrorConstant.PATTERN, errorField, errorArguments[2]));
                    break;
                default:
                    errors.add(errorFieldValidationCode + " " + defaultMessage);
            }
        } catch (Exception e) {
            errors.add(errorFieldValidationCode + " " + defaultMessage);
        }
    }

    private GlobalExceptionResponse handleBeanValidationException(MethodArgumentNotValidException ex, BindException ex1) {
        logger.error(ex == null ? ex1.getClass().getName() : ex.getClass().getName());
        List<FieldError> fieldErrors = ex == null ? ex1.getBindingResult().getFieldErrors() : ex.getBindingResult().getFieldErrors();

        String message = null;
        final List<String> errors = new ArrayList<>();
        for (final FieldError error : fieldErrors) {
            message = beanValidationExceptionMessage(error, message, errors);
        }
        for (final ObjectError error : ex == null ? ex1.getBindingResult().getGlobalErrors() : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return new GlobalExceptionResponse(ResponseStatus.FAILURE, httpStatus.value(), message, errors);
    }

    String beanValidationExceptionMessage(FieldError error, String message, List<String> errors) {
        String code = error.getCode();
        String field = error.getField();
        Object[] arguments = error.getArguments();

        if (code != null) {
            switch (code) {
                case ErrorCodeConstants.NOT_NULL:
                case ErrorCodeConstants.NOT_EMPTY:
                    message = handleNotNullOrNotEmpty(code, field, arguments, message, errors);
                    break;
                case ErrorCodeConstants.PATTERN:
                case ErrorCodeConstants.SIZE:
                    message = handleOtherValidation(code, field, arguments, message, errors);
                    break;
                default:
                    errors.add(field + ": " + error.getDefaultMessage());
            }
        }

        return message;
    }

    private String handleNotNullOrNotEmpty(String code, String field, Object[] arguments, String message, List<String> errors) {
        String notBlankCode = code.equals(ErrorCodeConstants.NOT_NULL) ? FieldErrorConstant.NOT_NULL : FieldErrorConstant.NOT_BLANK;
        String formattedField = Capital.capitalizeWord(field);

        if (arguments != null && arguments.length > 0) {
            String errorMessage = customMessageSource.get(notBlankCode, field, arguments[0]);
            errors.add(errorMessage);

            message = customMessageSource.get(notBlankCode, formattedField, arguments[0]);
        }
        return message;
    }

    private String handleOtherValidation(String code, String field, Object[] arguments, String message, List<String> errors) {
        if (arguments != null && arguments.length > 1) {
            String formattedValue = (String) (code.equals(ErrorCodeConstants.PATTERN) ? arguments[2] : arguments[1]);
            String errorCode = code.toLowerCase();
            errors.add(customMessageSource.get(errorCode, field, formattedValue));
            message = customMessageSource.get(errorCode, field, formattedValue);
        }
        return message;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        String rootCauseMessage = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        String userMessage;
        if (rootCauseMessage.contains("unique_user_email")) {
            userMessage = customMessageSource.get(FieldErrorConstant.UNIQUE_EMAIL);
        } else if (rootCauseMessage.contains("unique_user_mobile_number")) {
            userMessage = customMessageSource.get(FieldErrorConstant.UNIQUE_MOBILE);
        } else {
            userMessage = customMessageSource.get("constraint.violation");
        }

        GlobalExceptionResponse response = new GlobalExceptionResponse(ResponseStatus.FAILURE, httpStatus.value(), userMessage, rootCauseMessage);
        return new ResponseEntity<>(response, httpStatus);
    }

}
