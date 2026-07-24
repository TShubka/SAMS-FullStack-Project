package com.group6.sams.controller;

import com.group6.sams.dto.response.CourseGradeResponse;
import com.group6.sams.dto.response.GpaResponse;
import com.group6.sams.security.OwnershipService;
import com.group6.sams.security.UserPrincipal;
import com.group6.sams.service.GradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Computed grades and GPA. Owner: Member 3.
 *
 * Read-only by design: there is no endpoint to set a grade, because grades are
 * derived from marks rather than stored. Changing a grade means changing a mark,
 * which goes through MarkController and its authorization.
 */
@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;
    private final OwnershipService ownership;

    public GradeController(GradeService gradeService, OwnershipService ownership) {
        this.gradeService = gradeService;
        this.ownership = ownership;
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<List<CourseGradeResponse>> byStudent(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(gradeService.gradesForStudent(studentId, caller));
    }

    /** The signed-in student's own results, resolved from the token. */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CourseGradeResponse>> myGrades(
            @AuthenticationPrincipal UserPrincipal caller) {
        Long studentId = ownership.requireStudent(caller).getId();
        return ResponseEntity.ok(gradeService.gradesForStudent(studentId, caller));
    }

    @GetMapping("/enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<CourseGradeResponse> byEnrollment(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(gradeService.gradeForEnrollment(enrollmentId, caller));
    }

    /** Semester GPA when semester and academicYear are supplied, cumulative otherwise. */
    @GetMapping("/gpa/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<GpaResponse> gpa(
            @PathVariable Long studentId,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String academicYear,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(
                gradeService.gpa(studentId, semester, academicYear, caller));
    }

    @GetMapping("/gpa/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<GpaResponse> myGpa(
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) String academicYear,
            @AuthenticationPrincipal UserPrincipal caller) {
        Long studentId = ownership.requireStudent(caller).getId();
        return ResponseEntity.ok(
                gradeService.gpa(studentId, semester, academicYear, caller));
    }
}
