package com.group6.sams.exception;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/** Uniform error body returned by GlobalExceptionHandler for every failure. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    /** Present only for validation failures: field name -> message. */
    private Map<String, String> fieldErrors;
}
