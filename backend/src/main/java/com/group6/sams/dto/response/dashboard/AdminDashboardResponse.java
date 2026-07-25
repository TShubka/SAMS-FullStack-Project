package com.group6.sams.dto.response.dashboard;

import com.group6.sams.dto.response.report.StudentsByDepartmentReport;
import lombok.*;

import java.util.List;

/**
 * Admin dashboard. Owner: Member 4.
 *
 * Every figure is a live count from the database - there are no hard-coded or
 * sample numbers anywhere in this object. That is the "no fake dashboard data"
 * requirement made concrete.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    private long totalStudents;
    private long totalTeachers;
    private long totalDepartments;
    private long totalCourses;
    private long totalEnrollments;

    private long lowAttendanceCount;

    private List<StudentsByDepartmentReport.Row> studentsByDepartment;
}
