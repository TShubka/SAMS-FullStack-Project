package com.group8.sams.service;

import com.group8.sams.dto.response.AttendanceSummaryResponse;
import com.group8.sams.dto.response.report.*;
import com.group8.sams.security.UserPrincipal;

import java.math.BigDecimal;
import java.util.List;

public interface ReportService {

    StudentsByDepartmentReport studentsByDepartment();

    StudentPerformanceReport studentPerformance(Long studentId, UserPrincipal caller);

    List<AttendanceSummaryResponse> attendanceByCourse(Long courseId, UserPrincipal caller);

    List<AttendanceSummaryResponse> lowAttendance(BigDecimal threshold, UserPrincipal caller);

    CoursePerformanceReport coursePerformance(Long courseId, UserPrincipal caller);

    GradeDistributionReport gradeDistribution(Long courseId, UserPrincipal caller);

    PassFailReport passFail(Long courseId, UserPrincipal caller);

    DepartmentPerformanceReport departmentPerformance(Long departmentId);
}
