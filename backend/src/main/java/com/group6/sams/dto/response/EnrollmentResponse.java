package com.group6.sams.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {

    private Long id;
    private Integer semester;
    private String academicYear;
    private String status;
    private LocalDate enrolledOn;

    private Long studentId;
    private String studentName;
    private String rollNumber;

    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private Integer credits;
}
