package com.group8.sams.dto.response.report;

import com.group8.sams.dto.response.CourseGradeResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePerformanceReport {

    private Long courseId;
    private String courseCode;
    private String courseTitle;

    private long enrolled;
    private long graded;

    /** Average weighted percentage across graded students; null when none graded. */
    private BigDecimal averagePercentage;
    private BigDecimal highestPercentage;
    private BigDecimal lowestPercentage;

    private List<CourseGradeResponse> students;
}
