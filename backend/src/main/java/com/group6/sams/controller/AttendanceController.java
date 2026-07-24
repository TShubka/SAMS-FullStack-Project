package com.group6.sams.controller;

import com.group6.sams.dto.request.AttendanceRequest;
import com.group6.sams.dto.request.BulkAttendanceRequest;
import com.group6.sams.dto.response.AttendanceResponse;
import com.group6.sams.dto.response.AttendanceSummaryResponse;
import com.group6.sams.security.UserPrincipal;
import com.group6.sams.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Attendance endpoints. Owner: Member 3.
 *
 * @PreAuthorize handles the role half of authorization. The ownership half - is
 * this the teacher assigned to the course, is this the student's own record - is
 * enforced in AttendanceService, which is the only layer that can load the record
 * and compare it against the caller.
 *
 * ROLE_STUDENT appears on no write mapping here.
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<AttendanceResponse> record(
            @Valid @RequestBody AttendanceRequest request,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(attendanceService.record(request, caller));
    }

    /** Marks a whole class for one date in a single transaction. */
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> recordBulk(
            @Valid @RequestBody BulkAttendanceRequest request,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(attendanceService.recordBulk(request, caller));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<AttendanceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequest request,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(attendanceService.update(id, request, caller));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserPrincipal caller) {
        attendanceService.delete(id, caller);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> byCourse(
            @PathVariable Long courseId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(attendanceService.findByCourseAndDate(courseId, date, caller));
    }

    /** A student may pass only their own id; anything else is refused with 403. */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','STUDENT')")
    public ResponseEntity<List<AttendanceResponse>> byStudent(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(attendanceService.findByStudent(studentId, caller));
    }

    @GetMapping("/percentage")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','STUDENT')")
    public ResponseEntity<AttendanceSummaryResponse> percentage(
            @RequestParam Long studentId,
            @RequestParam Long courseId,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(attendanceService.percentage(studentId, courseId, caller));
    }

    @GetMapping("/summary/course/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<List<AttendanceSummaryResponse>> courseSummary(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(attendanceService.courseSummary(courseId, caller));
    }

    /** Teachers see their own courses only; admins see everything. */
    @GetMapping("/low")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<List<AttendanceSummaryResponse>> lowAttendance(
            @RequestParam(required = false) BigDecimal threshold,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(attendanceService.lowAttendance(threshold, caller));
    }
}
