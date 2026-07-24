package com.group6.sams.exception;

/**
 * Maps to HTTP 403. Raised when the caller is authenticated and holds the right
 * role but is not the owner of the resource - a teacher touching another
 * teacher's course, or a student reading another student's records.
 *
 * We return 403 rather than 404 here: the resource exists, the caller simply may
 * not have it.
 */
public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}
