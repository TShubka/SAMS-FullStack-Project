package com.group6.sams.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentRequest {

    @NotNull(message = "Student id is required")
    private Long studentId;

    @NotNull(message = "Course id is required")
    private Long courseId;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be between 1 and 12")
    @Max(value = 12, message = "Semester must be between 1 and 12")
    private Integer semester;

    @NotBlank(message = "Academic year is required")
    @Pattern(regexp = "^\\d{4}-\\d{4}$",
             message = "Academic year must look like 2025-2026")
    private String academicYear;

    /** ACTIVE, COMPLETED or DROPPED. Defaults to ACTIVE when omitted. */
    @Pattern(regexp = "^(ACTIVE|COMPLETED|DROPPED)$",
             message = "Status must be ACTIVE, COMPLETED or DROPPED")
    private String status;
}
