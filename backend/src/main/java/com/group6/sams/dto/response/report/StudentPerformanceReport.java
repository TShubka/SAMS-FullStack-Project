package com.group6.sams.dto.response.report;

import com.group6.sams.dto.response.CourseGradeResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * One student's academic performance across all their courses: their grades, GPA
 * and pass/fail counts. Consumed by the student dashboard and by admins.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentPerformanceReport {

    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String departmentName;

    private BigDecimal cumulativeGpa;
    private int totalCourses;
    private int gradedCourses;
    private long passed;
    private long failed;

    private List<CourseGradeResponse> courses;
}
