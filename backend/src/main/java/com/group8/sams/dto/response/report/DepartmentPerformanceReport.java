package com.group8.sams.dto.response.report;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentPerformanceReport {

    private Long departmentId;
    private String departmentName;
    private String departmentCode;

    private long totalStudents;
    private long studentsWithGpa;

    /** Average cumulative GPA across students who have any graded work; null if none. */
    private BigDecimal averageGpa;
    private BigDecimal highestGpa;
    private BigDecimal lowestGpa;

    private long totalCourses;
}
