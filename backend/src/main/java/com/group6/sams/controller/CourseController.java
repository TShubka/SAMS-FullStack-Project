package com.group6.sams.controller;

import com.group6.sams.dto.request.CourseRequest;
import com.group6.sams.dto.response.CourseResponse;
import com.group6.sams.dto.response.PageResponse;
import com.group6.sams.security.UserPrincipal;
import com.group6.sams.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** Course CRUD and filtering. Owner: Member 2. */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<PageResponse<CourseResponse>> findAll(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "code", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok(
                courseService.findAll(departmentId, semester, teacherId, search, pageable));
    }

    /**
     * Courses assigned to the calling teacher.
     *
     * The teacher is resolved from the token, so this route cannot be used to read
     * another teacher's assignments by changing a parameter.
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<CourseResponse>> myCourses(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(courseService.findMyCourses(principal.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<CourseResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        CourseResponse created = courseService.create(request);
        return ResponseEntity.created(URI.create("/api/courses/" + created.getId()))
                             .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> update(
            @PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.update(id, request));
    }

    /** 409 when students are still enrolled; assessments are removed with the course. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
