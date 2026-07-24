package com.group6.sams.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {

    private Long id;
    private LocalDate attendanceDate;
    private String status;
    private String remarks;

    private Long enrollmentId;

    private Long studentId;
    private String studentName;
    private String rollNumber;

    private Long courseId;
    private String courseCode;
    private String courseTitle;

    /** Null once the recording teacher has been removed - the record still stands. */
    private String recordedBy;
}
