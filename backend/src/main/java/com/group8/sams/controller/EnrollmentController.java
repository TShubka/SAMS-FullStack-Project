package com.group8.sams.controller;

import com.group8.sams.dto.request.EnrollmentRequest;
import com.group8.sams.dto.response.EnrollmentResponse;
import com.group8.sams.dto.response.PageResponse;
import com.group8.sams.security.UserPrincipal;
import com.group8.sams.service.EnrollmentService;
import com.group8.sams.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** Enrollment CRUD. Owner: Member 2. */
@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final StudentService studentService;

    public EnrollmentController(EnrollmentService enrollmentService,
                                StudentService studentService) {
        this.enrollmentService = enrollmentService;
        this.studentService = studentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PageResponse<EnrollmentResponse>> findAll(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(enrollmentService.findAll(pageable));
    }

    /** The signed-in student's own enrollments, resolved from the token. */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<EnrollmentResponse>> myEnrollments(
            @AuthenticationPrincipal UserPrincipal principal) {
        Long studentId = studentService.findByUserId(principal.getId()).getId();
        return ResponseEntity.ok(enrollmentService.findByStudent(studentId));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<EnrollmentResponse>> byStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.findByStudent(studentId));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<EnrollmentResponse>> byCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.findByCourse(courseId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<EnrollmentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.findById(id));
    }

    /** 409 when the same student is already enrolled for that course, semester and year. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentResponse> create(
            @Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse created = enrollmentService.create(request);
        return ResponseEntity.created(URI.create("/api/enrollments/" + created.getId()))
                             .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentResponse> update(
            @PathVariable Long id, @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(enrollmentService.update(id, request));
    }

    /** Cascades to this enrollment's attendance and marks. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
