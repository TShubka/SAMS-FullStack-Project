package com.group6.sams.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Credit-weighted GPA for a student.
 *
 * When `semester` and `academicYear` are set this is a semester GPA; when they are
 * null it is the cumulative GPA across everything graded so far.
 *
 * gpa is null - not 0.00 - when nothing has been graded. Reporting 0.00 would read
 * as total failure rather than "no results yet".
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpaResponse {

    private Long studentId;
    private String studentName;
    private String rollNumber;

    /** Null on a cumulative GPA. */
    private Integer semester;
    private String academicYear;

    private BigDecimal gpa;

    /** Credits that actually contributed - graded courses only. */
    private int totalCredits;
    private int gradedCourses;
    private int ungradedCourses;

    private List<CourseGradeResponse> courses;
}
