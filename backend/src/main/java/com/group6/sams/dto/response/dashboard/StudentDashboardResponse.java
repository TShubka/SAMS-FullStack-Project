package com.group6.sams.dto.response.dashboard;

import com.group6.sams.dto.response.CourseGradeResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/** Student dashboard - the caller's own academic summary. Owner: Member 4. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDashboardResponse {

    private String studentName;
    private String rollNumber;
    private String departmentName;
    private Integer currentSemester;

    private long enrolledCourses;
    private BigDecimal cumulativeGpa;
    private long coursesPassed;

    /** Courses currently below the attendance threshold, so the student sees a warning. */
    private long lowAttendanceCourses;

    private List<CourseGradeResponse> courses;
}
