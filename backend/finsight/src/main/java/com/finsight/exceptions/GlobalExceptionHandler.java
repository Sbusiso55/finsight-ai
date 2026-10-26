
package com.finsight.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentials(
            InvalidCredentialsException exception) {

        logger.warn("Login attempt failed: invalid credentials");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid email or password");
    }


    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException exception) {

        logger.warn("Database constraint violation: {}", exception.getMostSpecificCause().getMessage());

        String message = "The information provided is invalid or conflicts with existing data.";

        String cause = exception.getMostSpecificCause().getMessage();

        if (cause != null && cause.contains("value too long")) {
            message = "One or more fields exceed the allowed length.";
        } else if (cause != null && cause.contains("not-null constraint")) {
            message = "A required field is missing.";
        } else if (cause != null && cause.contains("duplicate key")) {
            message = "This record already exists.";
        }

        return ResponseEntity.badRequest().body(message);
    }

}
