package com.group6.sams.repository.projection;

/**
 * Generic "label -> count" row for grouped reports. Owner: Member 4.
 *
 * A Spring Data projection interface: the query selects aliases matching these
 * getters and Spring materializes them without a concrete class. This keeps the
 * aggregation in the database - the alternative, findAll() followed by a Java
 * groupingBy, would pull whole tables into memory (NFR-08).
 */
public interface CountByLabel {

    String getLabel();

    long getCount();
}
