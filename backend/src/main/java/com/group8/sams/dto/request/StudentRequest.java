package com.group8.sams.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequest {

    /** The login account this profile belongs to. Required on create only. */
    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Department id is required")
    private Long departmentId;

    @NotBlank(message = "Roll number is required")
    @Size(max = 20, message = "Roll number must not exceed 20 characters")
    private String rollNumber;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @NotNull(message = "Admission year is required")
    @Min(value = 2000, message = "Admission year must be 2000 or later")
    @Max(value = 2100, message = "Admission year must be 2100 or earlier")
    private Integer admissionYear;

    @NotNull(message = "Current semester is required")
    @Min(value = 1, message = "Semester must be between 1 and 12")
    @Max(value = 12, message = "Semester must be between 1 and 12")
    private Integer currentSemester;

    @Size(max = 20)
    private String phone;
}
