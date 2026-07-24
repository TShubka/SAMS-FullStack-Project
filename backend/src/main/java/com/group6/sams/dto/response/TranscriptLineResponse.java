package com.group6.sams.dto.response;

import lombok.*;

import java.math.BigDecimal;

/** One course row on a transcript. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranscriptLineResponse {

    private String courseCode;
    private String courseTitle;
    private Integer credits;
    private Integer semester;
    private String academicYear;

    private BigDecimal percentage;
    private String grade;
    private BigDecimal gradePoints;
    private String status;
}
