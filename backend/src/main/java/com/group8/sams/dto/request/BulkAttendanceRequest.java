package com.group8.sams.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Marks a whole class for one date in a single transaction.
 *
 * This is how attendance is actually taken - a teacher works down the roster once,
 * not one HTTP request per student. Doing it in one transaction also means a
 * failure half way through does not leave the register partially filled.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkAttendanceRequest {

    @NotNull(message = "Course id is required")
    private Long courseId;

    @NotNull(message = "Attendance date is required")
    @PastOrPresent(message = "Attendance cannot be recorded for a future date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate attendanceDate;

    /** @Valid cascades validation into each entry rather than only the list itself. */
    @NotEmpty(message = "At least one attendance entry is required")
    @Valid
    private List<Entry> entries;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Entry {

        @NotNull(message = "Enrollment id is required")
        private Long enrollmentId;

        @NotBlank(message = "Status is required")
        @Pattern(regexp = "^(PRESENT|ABSENT|LATE)$",
                 message = "Status must be PRESENT, ABSENT or LATE")
        private String status;

        @Size(max = 255)
        private String remarks;
    }
}
