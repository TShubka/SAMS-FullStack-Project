package com.group6.sams.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRequest {

    @NotNull(message = "Enrollment id is required")
    private Long enrollmentId;

    /**
     * @PastOrPresent enforces the "no attendance for a future date" rule
     * declaratively, so it is reported as a normal 400 alongside other field errors.
     */
    @NotNull(message = "Attendance date is required")
    @PastOrPresent(message = "Attendance cannot be recorded for a future date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate attendanceDate;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(PRESENT|ABSENT|LATE)$",
             message = "Status must be PRESENT, ABSENT or LATE")
    private String status;

    @Size(max = 255, message = "Remarks must not exceed 255 characters")
    private String remarks;
}
