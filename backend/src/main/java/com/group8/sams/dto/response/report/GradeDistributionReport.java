package com.group8.sams.dto.response.report;

import lombok.*;

import java.util.List;

/**
 * How many students fall into each letter grade for a course.
 *
 * Grades are bucketed in the service by GradeUtil rather than by a SQL CASE
 * expression: putting the boundaries in SQL would duplicate the grade scale and
 * let the report drift out of step with the marks page. The aggregation is still
 * done over derived grades, not by loading raw rows into a Java loop for the sums.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeDistributionReport {

    private Long courseId;
    private String courseCode;
    private String courseTitle;

    private long gradedStudents;
    private long ungradedStudents;

    /** One bucket per letter grade, in scale order, zero-filled. */
    private List<Bucket> distribution;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Bucket {
        private String grade;
        private long count;
    }
}
