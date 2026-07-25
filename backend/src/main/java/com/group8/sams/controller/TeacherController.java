package com.group8.sams.controller;

import com.group8.sams.dto.request.TeacherRequest;
import com.group8.sams.dto.response.PageResponse;
import com.group8.sams.dto.response.TeacherResponse;
import com.group8.sams.security.UserPrincipal;
import com.group8.sams.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/** Teacher CRUD. Owner: Member 2. */
@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<PageResponse<TeacherResponse>> findAll(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "employeeCode", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok(teacherService.findAll(departmentId, search, pageable));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(teacherService.findByUserId(principal.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<TeacherResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> create(@Valid @RequestBody TeacherRequest request) {
        TeacherResponse created = teacherService.create(request);
        return ResponseEntity.created(URI.create("/api/teachers/" + created.getId()))
                             .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> update(
            @PathVariable Long id, @Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.ok(teacherService.update(id, request));
    }

    /** Unassigns the teacher's courses and clears attribution rather than deleting records. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
