package com.group6.sams.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * A student's computed result for one course.
 *
 * Nothing here is stored. Everything is derived from the marks on demand, so the
 * grade can never drift out of sync with the scores that produced it.
 *
 * percentage, grade, gradePoints and passed are all null together when no marks
 * have been entered yet - "not graded" is not "failed".
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseGradeResponse {

    private Long enrollmentId;

    private Long studentId;
    private String studentName;
    private String rollNumber;

    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private Integer credits;
    private Integer semester;
    private String academicYear;

    /** Weighted score accumulated so far and the weight it was measured against. */
    private BigDecimal weightedScore;
    private BigDecimal recordedWeight;

    private BigDecimal percentage;
    private String grade;
    private BigDecimal gradePoints;
    private Boolean passed;

    private List<MarkResponse> marks;
}
