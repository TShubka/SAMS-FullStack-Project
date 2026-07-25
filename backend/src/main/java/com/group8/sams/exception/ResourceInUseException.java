package com.group8.sams.exception;

/**
 * Maps to HTTP 409. Raised when a delete is refused because other records still
 * reference the target - the RESTRICT rules from the Phase 1 design, enforced in
 * the service layer because Hibernate's schema generation does not emit
 * ON DELETE clauses.
 *
 * Distinct from BusinessRuleException (400): the request itself is well-formed,
 * it simply conflicts with the current state of the data.
 */
public class ResourceInUseException extends RuntimeException {

    public ResourceInUseException(String message) {
        super(message);
    }
}
