package com.group6.sams.exception;

/** Maps to HTTP 409. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resource, String field, Object value) {
        super("%s already exists with %s: '%s'".formatted(resource, field, value));
    }
}
