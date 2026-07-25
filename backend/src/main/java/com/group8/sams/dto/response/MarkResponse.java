package com.group8.sams.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarkResponse {

    private Long id;
    private BigDecimal marksObtained;
    private BigDecimal maxMarks;

    /** marksObtained as a percentage of this assessment's maximum. */
    private BigDecimal percentage;

    private Long assessmentId;
    private String assessmentTitle;
    private String assessmentType;
    private BigDecimal weightPercent;

    private Long enrollmentId;
    private Long studentId;
    private String studentName;
    private String rollNumber;

    private Long courseId;
    private String courseCode;

    /** Null once the entering teacher has been removed - the mark still stands. */
    private String enteredBy;
    private LocalDateTime enteredAt;
}
