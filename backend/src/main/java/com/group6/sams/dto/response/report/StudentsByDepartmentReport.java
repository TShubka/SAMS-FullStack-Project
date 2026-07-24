package com.group6.sams.dto.response.report;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentsByDepartmentReport {

    private long totalStudents;
    private List<Row> departments;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Row {
        private Long departmentId;
        private String departmentName;
        private String departmentCode;
        private long studentCount;
    }
}
