package com.group8.sams.controller;

import com.group8.sams.dto.response.TranscriptResponse;
import com.group8.sams.security.OwnershipService;
import com.group8.sams.security.UserPrincipal;
import com.group8.sams.service.TranscriptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Academic transcript. Owner: Member 4. */
@RestController
@RequestMapping("/api/transcripts")
public class TranscriptController {

    private final TranscriptService transcriptService;
    private final OwnershipService ownership;

    public TranscriptController(TranscriptService transcriptService,
                                OwnershipService ownership) {
        this.transcriptService = transcriptService;
        this.ownership = ownership;
    }

    /** A student passing another student's id is refused with 403 in the service. */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<TranscriptResponse> byStudent(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(transcriptService.forStudent(studentId, caller));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<TranscriptResponse> mine(
            @AuthenticationPrincipal UserPrincipal caller) {
        Long studentId = ownership.requireStudent(caller).getId();
        return ResponseEntity.ok(transcriptService.forStudent(studentId, caller));
    }
}
