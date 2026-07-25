package com.group8.sams.controller;

import com.group8.sams.dto.request.MarkRequest;
import com.group8.sams.dto.response.MarkResponse;
import com.group8.sams.security.UserPrincipal;
import com.group8.sams.service.MarkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Mark entry. Owner: Member 3.
 *
 * ROLE_STUDENT appears on no write mapping in this file, and SecurityConfig
 * refuses it again at the filter chain. That double refusal is the answer to
 * "why can't students modify their own marks?" - the button being hidden in React
 * is convenience, not the control.
 */
@RestController
@RequestMapping("/api/marks")
public class MarkController {

    private final MarkService markService;

    public MarkController(MarkService markService) {
        this.markService = markService;
    }

    /** 400 if above maxMarks or cross-course, 409 if already scored. */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<MarkResponse> create(
            @Valid @RequestBody MarkRequest request,
            @AuthenticationPrincipal UserPrincipal caller) {
        MarkResponse created = markService.create(request, caller);
        return ResponseEntity.created(URI.create("/api/marks/" + created.getId()))
                             .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<MarkResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MarkRequest request,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(markService.update(id, request, caller));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserPrincipal caller) {
        markService.delete(id, caller);
        return ResponseEntity.noContent().build();
    }

    /** A student may read only their own enrollment's marks. */
    @GetMapping("/enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<List<MarkResponse>> byEnrollment(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(markService.findByEnrollment(enrollmentId, caller));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<MarkResponse>> byCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(markService.findByCourse(courseId, caller));
    }
}
