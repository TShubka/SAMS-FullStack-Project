package com.group6.sams.exception;

/**
 * Maps to HTTP 400. Raised by services for rules the database cannot express -
 * marks above an assessment's maximum, assessment weights exceeding 100 percent,
 * a mark whose enrollment and assessment belong to different courses.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
