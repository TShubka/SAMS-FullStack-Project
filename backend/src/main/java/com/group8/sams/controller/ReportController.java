package com.group8.sams.controller;

import com.group8.sams.dto.response.AttendanceSummaryResponse;
import com.group8.sams.dto.response.report.*;
import com.group8.sams.security.UserPrincipal;
import com.group8.sams.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Academic and department reports. Owner: Member 4.
 *
 * Course-scoped reports are open to the owning teacher and admin; the ownership
 * check inside each service call refuses a teacher asking about a course they do
 * not teach. Department- and institution-wide reports are ADMIN only.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/students-by-department")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentsByDepartmentReport> studentsByDepartment() {
        return ResponseEntity.ok(reportService.studentsByDepartment());
    }

    @GetMapping("/student-performance/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<StudentPerformanceReport> studentPerformance(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(reportService.studentPerformance(studentId, caller));
    }

    @GetMapping("/attendance/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<AttendanceSummaryResponse>> attendanceByCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(reportService.attendanceByCourse(courseId, caller));
    }

    @GetMapping("/low-attendance")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<AttendanceSummaryResponse>> lowAttendance(
            @RequestParam(required = false) BigDecimal threshold,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(reportService.lowAttendance(threshold, caller));
    }

    @GetMapping("/course-performance/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<CoursePerformanceReport> coursePerformance(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(reportService.coursePerformance(courseId, caller));
    }

    @GetMapping("/grade-distribution")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<GradeDistributionReport> gradeDistribution(
            @RequestParam Long courseId,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(reportService.gradeDistribution(courseId, caller));
    }

    @GetMapping("/pass-fail")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PassFailReport> passFail(
            @RequestParam Long courseId,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(reportService.passFail(courseId, caller));
    }

    @GetMapping("/department-performance/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentPerformanceReport> departmentPerformance(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(reportService.departmentPerformance(departmentId));
    }
}
