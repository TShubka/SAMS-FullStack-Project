package com.group8.sams.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentResponse {

    private Long id;
    private String title;
    private String type;
    private BigDecimal maxMarks;
    private BigDecimal weightPercent;
    private LocalDate assessedOn;

    private Long courseId;
    private String courseCode;
    private String courseTitle;
}
