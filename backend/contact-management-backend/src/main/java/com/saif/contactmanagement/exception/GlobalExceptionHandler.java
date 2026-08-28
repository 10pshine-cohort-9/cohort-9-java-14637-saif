package com.saif.contactmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String STATUS_KEY = "status";
    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_KEY = "message";

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex) {

        log.warn("Email already exists exception: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();

        response.put(TIMESTAMP_KEY, LocalDateTime.now(ZoneId.systemDefault()));
        response.put(STATUS_KEY, HttpStatus.CONFLICT.value());
        response.put(ERROR_KEY, HttpStatus.CONFLICT.getReasonPhrase());
        response.put(MESSAGE_KEY, ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex) {

        log.warn("Resource not found exception: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();

        response.put(TIMESTAMP_KEY, LocalDateTime.now(ZoneId.systemDefault()));
        response.put(STATUS_KEY, HttpStatus.NOT_FOUND.value());
        response.put(ERROR_KEY, HttpStatus.NOT_FOUND.getReasonPhrase());
        response.put(MESSAGE_KEY, ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation failed: {}", errors);

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP_KEY, LocalDateTime.now(ZoneId.systemDefault()));
        response.put(STATUS_KEY, status.value());
        response.put(ERROR_KEY, HttpStatus.valueOf(status.value()).getReasonPhrase());
        response.put("errors", errors);

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {

        log.warn("Illegal argument exception: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();

        response.put(TIMESTAMP_KEY, LocalDateTime.now(ZoneId.systemDefault()));
        response.put(STATUS_KEY, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR_KEY, HttpStatus.BAD_REQUEST.getReasonPhrase());
        response.put(MESSAGE_KEY, ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            org.springframework.security.core.AuthenticationException ex) {

        log.warn("Authentication exception occurred: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();

        response.put(TIMESTAMP_KEY, LocalDateTime.now(ZoneId.systemDefault()));
        response.put(STATUS_KEY, HttpStatus.UNAUTHORIZED.value());
        response.put(ERROR_KEY, HttpStatus.UNAUTHORIZED.getReasonPhrase());
        response.put(MESSAGE_KEY, ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.warn("Http message not readable. Event: HTTP_MESSAGE_READ_FAIL, ExceptionType: {}", ex.getClass().getSimpleName());

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP_KEY, LocalDateTime.now(ZoneId.systemDefault()));
        response.put(STATUS_KEY, status.value());
        response.put(ERROR_KEY, HttpStatus.valueOf(status.value()).getReasonPhrase());
        response.put(MESSAGE_KEY, "Required request body is missing or invalid");

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllUncaughtException(Exception ex) {
        log.error("An unexpected error occurred: ", ex);

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP_KEY, LocalDateTime.now(ZoneId.systemDefault()));
        response.put(STATUS_KEY, HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put(ERROR_KEY, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        response.put(MESSAGE_KEY, "An internal server error occurred.");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}