package com.group8.sams.controller;

import com.group8.sams.dto.response.dashboard.AdminDashboardResponse;
import com.group8.sams.dto.response.dashboard.StudentDashboardResponse;
import com.group8.sams.dto.response.dashboard.TeacherDashboardResponse;
import com.group8.sams.security.UserPrincipal;
import com.group8.sams.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Role-based dashboards. Owner: Member 4. Each is restricted to its own role. */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> admin(
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(dashboardService.admin(caller));
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherDashboardResponse> teacher(
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(dashboardService.teacher(caller));
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentDashboardResponse> student(
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(dashboardService.student(caller));
    }
}
