package com.group8.sams.mapper;

import com.group8.sams.dto.response.AssessmentResponse;
import com.group8.sams.dto.response.MarkResponse;
import com.group8.sams.entity.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Owner: Member 3. Must be called inside a transaction - associations are LAZY. */
public final class GradeMapper {

    private GradeMapper() {
    }

    public static AssessmentResponse toResponse(Assessment a) {
        Course c = a.getCourse();
        return AssessmentResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .type(a.getType().name())
                .maxMarks(a.getMaxMarks())
                .weightPercent(a.getWeightPercent())
                .assessedOn(a.getAssessedOn())
                .courseId(c.getId())
                .courseCode(c.getCode())
                .courseTitle(c.getTitle())
                .build();
    }

    public static MarkResponse toResponse(Mark m) {
        Assessment a = m.getAssessment();
        Enrollment e = m.getEnrollment();
        Student s = e.getStudent();
        Course c = e.getCourse();
        Teacher enteredBy = m.getEnteredBy();

        BigDecimal percentage = null;
        if (a.getMaxMarks() != null && a.getMaxMarks().compareTo(BigDecimal.ZERO) > 0) {
            percentage = m.getMarksObtained()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(a.getMaxMarks(), 2, RoundingMode.HALF_UP);
        }

        return MarkResponse.builder()
                .id(m.getId())
                .marksObtained(m.getMarksObtained())
                .maxMarks(a.getMaxMarks())
                .percentage(percentage)
                .assessmentId(a.getId())
                .assessmentTitle(a.getTitle())
                .assessmentType(a.getType().name())
                .weightPercent(a.getWeightPercent())
                .enrollmentId(e.getId())
                .studentId(s.getId())
                .studentName(s.getFullName())
                .rollNumber(s.getRollNumber())
                .courseId(c.getId())
                .courseCode(c.getCode())
                .enteredBy(enteredBy != null ? enteredBy.getFullName() : null)
                .enteredAt(m.getEnteredAt())
                .build();
    }
}
