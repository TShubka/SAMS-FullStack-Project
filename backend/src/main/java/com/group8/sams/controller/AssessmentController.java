package com.group8.sams.controller;

import com.group8.sams.dto.request.AssessmentRequest;
import com.group8.sams.dto.response.AssessmentResponse;
import com.group8.sams.security.UserPrincipal;
import com.group8.sams.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** Assessment definitions. Owner: Member 3. */
@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /** Students may read this - they need to know what each assessment is worth. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<List<AssessmentResponse>> byCourse(
            @RequestParam Long courseId,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(assessmentService.findByCourse(courseId, caller));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<AssessmentResponse> findById(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(assessmentService.findById(id, caller));
    }

    /** 400 when the course's total assessment weight would exceed 100%. */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<AssessmentResponse> create(
            @Valid @RequestBody AssessmentRequest request,
            @AuthenticationPrincipal UserPrincipal caller) {
        AssessmentResponse created = assessmentService.create(request, caller);
        return ResponseEntity.created(URI.create("/api/assessments/" + created.getId()))
                             .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<AssessmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AssessmentRequest request,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(assessmentService.update(id, request, caller));
    }

    /** 400 while marks exist for it - scores are never discarded silently. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserPrincipal caller) {
        assessmentService.delete(id, caller);
        return ResponseEntity.noContent().build();
    }
}
