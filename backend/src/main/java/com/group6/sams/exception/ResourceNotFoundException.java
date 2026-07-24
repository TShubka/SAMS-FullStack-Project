package com.group6.sams.exception;

/** Maps to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super("%s not found with %s: '%s'".formatted(resource, field, value));
    }
}
