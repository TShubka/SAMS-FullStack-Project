package com.group8.sams.dto.response;

import lombok.*;

import java.math.BigDecimal;

/**
 * Attendance figures for one student in one course.
 *
 * percentage is null when no attendance has been recorded yet. The frontend must
 * render that as "no records", never as 0%.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSummaryResponse {

    private Long studentId;
    private String studentName;
    private String rollNumber;

    private Long courseId;
    private String courseCode;
    private String courseTitle;

    private Long enrollmentId;

    private long totalSessions;
    private long presentCount;
    private long absentCount;
    private long lateCount;

    /** PRESENT + LATE, the numerator of the percentage. */
    private long attendedCount;

    private BigDecimal percentage;

    /** True only when a percentage exists and falls below the configured threshold. */
    private boolean belowThreshold;
}
