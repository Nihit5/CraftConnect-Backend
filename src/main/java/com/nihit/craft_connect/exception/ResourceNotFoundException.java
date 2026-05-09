package com.nihit.craft_connect.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author bkings
 * @version 1.0.0
 * @since 2023-12-14 00:51
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "404 Not Found")
public class ResourceNotFoundException extends RuntimeException {
}
