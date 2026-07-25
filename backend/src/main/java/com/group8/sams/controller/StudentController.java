package com.group8.sams.controller;

import com.group8.sams.dto.request.StudentRequest;
import com.group8.sams.dto.response.PageResponse;
import com.group8.sams.dto.response.StudentResponse;
import com.group8.sams.security.UserPrincipal;
import com.group8.sams.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/** Student CRUD, search and filtering. Owner: Member 2. */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PageResponse<StudentResponse>> findAll(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer admissionYear,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "rollNumber", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok(
                studentService.findAll(departmentId, admissionYear, search, pageable));
    }

    /**
     * The signed-in student's own profile.
     *
     * Declared before /{id} so that "me" is never parsed as an id, and resolved from
     * the token rather than a path variable - a student cannot ask for someone else's
     * profile through this route at all.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(studentService.findByUserId(principal.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<StudentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        StudentResponse created = studentService.create(request);
        return ResponseEntity.created(URI.create("/api/students/" + created.getId()))
                             .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> update(
            @PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.update(id, request));
    }

    /** Cascades to the student's enrollments, attendance and marks. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
