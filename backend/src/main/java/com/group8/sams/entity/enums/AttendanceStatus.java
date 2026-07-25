package com.group8.sams.entity.enums;

/**
 * Attendance state for one student on one date of one course.
 * Persisted as STRING (never ORDINAL) so reordering these constants
 * cannot corrupt existing rows.
 */
public enum AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE
}
