package com.group8.sams.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentRequest {

    @NotNull(message = "Course id is required")
    private Long courseId;

    @NotBlank(message = "Title is required")
    @Size(max = 100)
    private String title;

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "^(ASSIGNMENT|QUIZ|MIDTERM|FINAL)$",
             message = "Type must be ASSIGNMENT, QUIZ, MIDTERM or FINAL")
    private String type;

    @NotNull(message = "Maximum marks are required")
    @DecimalMin(value = "0.01", message = "Maximum marks must be greater than zero")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal maxMarks;

    /**
     * The service additionally checks that the weights of a course's assessments do
     * not exceed 100 in total - a rule no single-row constraint can express.
     */
    @NotNull(message = "Weight percent is required")
    @DecimalMin(value = "0.0", message = "Weight must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Weight must be between 0 and 100")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal weightPercent;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate assessedOn;
}
