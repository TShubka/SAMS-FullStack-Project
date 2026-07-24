package com.group6.sams.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequest {

    @NotBlank(message = "Course code is required")
    @Size(max = 20)
    private String code;

    @NotBlank(message = "Course title is required")
    @Size(max = 120)
    private String title;

    @NotNull(message = "Department id is required")
    private Long departmentId;

    /** Optional: a course may exist before a teacher is assigned to it. */
    private Long teacherId;

    @NotNull(message = "Credits are required")
    @Min(value = 1, message = "Credits must be between 1 and 10")
    @Max(value = 10, message = "Credits must be between 1 and 10")
    private Integer credits;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be between 1 and 12")
    @Max(value = 12, message = "Semester must be between 1 and 12")
    private Integer semester;
}
