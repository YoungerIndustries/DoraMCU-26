package com.yisy.modules.doramcu.fullthrust.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice("globalExceptionHandler_1")
    @RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<Map<String, Object>> handleNotWritable(HttpMessageNotWritableException ex) {
        log.error("HTTP message not writable: ", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "HttpMessageNotWritableException");
        body.put("message", ex.getMessage());
        Throwable cause = ex.getCause();
        while (cause != null) {
            body.put("cause", cause.getClass().getSimpleName() + ": " + cause.getMessage());
            cause = cause.getCause();
        }
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("Unhandled exception: ", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getClass().getSimpleName());
        body.put("message", ex.getMessage());
        Throwable cause = ex.getCause();
        while (cause != null) {
            body.put("cause", cause.getClass().getSimpleName() + ": " + cause.getMessage());
            cause = cause.getCause();
        }
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
