package com.group8.sams.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkRequest {

    @NotNull(message = "Enrollment id is required")
    private Long enrollmentId;

    @NotNull(message = "Assessment id is required")
    private Long assessmentId;

    /**
     * The lower bound is checked here and in the database. The upper bound cannot be
     * expressed as a constraint - it depends on the parent assessment's maxMarks -
     * so MarkService enforces it and returns 400.
     */
    @NotNull(message = "Marks obtained is required")
    @DecimalMin(value = "0.0", message = "Marks cannot be negative")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal marksObtained;
}
