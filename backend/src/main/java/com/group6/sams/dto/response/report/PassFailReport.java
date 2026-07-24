package com.group6.sams.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassFailReport {

    private Long courseId;
    private String courseCode;
    private String courseTitle;

    private long graded;
    private long passed;
    private long failed;

    /** Percentage of graded students who passed; null when none are graded. */
    private BigDecimal passRate;
}
