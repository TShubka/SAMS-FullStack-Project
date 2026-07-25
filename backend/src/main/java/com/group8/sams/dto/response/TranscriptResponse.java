package com.group8.sams.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * A full academic transcript, assembled on demand.
 *
 * This is NOT stored anywhere: it is derived from the student's enrollments, marks
 * and the grade scale each time it is requested. Storing it would duplicate data
 * that can go stale the moment a mark is corrected - which is exactly the
 * normalization point the design makes.
 *
 * Courses are grouped into semester blocks, each with its own GPA, and the whole
 * transcript carries the cumulative GPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranscriptResponse {

    private Long studentId;
    private String studentName;
    private String rollNumber;
    private Integer admissionYear;

    private String departmentName;
    private String departmentCode;

    private List<SemesterBlock> semesters;

    private int totalCredits;
    private int creditsEarned;
    private BigDecimal cumulativeGpa;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SemesterBlock {
        private Integer semester;
        private String academicYear;
        private List<TranscriptLineResponse> courses;
        private int semesterCredits;
        private BigDecimal semesterGpa;
    }
}
