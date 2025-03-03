package com.saymk.cloud6x.handler;

import com.saymk.cloud6x.exception.UserAlreadyExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Username or password invalid"));
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<?> handleConflict(UserAlreadyExistException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Username already taken."));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Username or password invalid"));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<?> handleConstraintViolationException(HandlerMethodValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid request"));
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<?> handleMissingCookieException(MissingRequestCookieException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Missing refresh token"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put("message", error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errors);
    }
}
