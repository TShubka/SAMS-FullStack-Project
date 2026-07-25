package com.group8.sams.service.impl;

import com.group8.sams.dto.response.CourseGradeResponse;
import com.group8.sams.dto.response.TranscriptLineResponse;
import com.group8.sams.dto.response.TranscriptResponse;
import com.group8.sams.entity.Student;
import com.group8.sams.security.OwnershipService;
import com.group8.sams.security.UserPrincipal;
import com.group8.sams.service.GradeService;
import com.group8.sams.service.TranscriptService;
import com.group8.sams.util.GradeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Assembles a transcript from a student's course grades. Owner: Member 4.
 *
 * It delegates every grade and GPA figure to GradeService/GradeUtil rather than
 * recomputing them, so the transcript, the marks page and the reports can never
 * show different numbers for the same student.
 *
 * A student may read only their own transcript; the ownership check enforces that
 * and returns 403 for anyone asking for someone else's.
 */
@Service
public class TranscriptServiceImpl implements TranscriptService {

    private final GradeService gradeService;
    private final OwnershipService ownership;

    public TranscriptServiceImpl(GradeService gradeService, OwnershipService ownership) {
        this.gradeService = gradeService;
        this.ownership = ownership;
    }

    @Override
    @Transactional(readOnly = true)
    public TranscriptResponse forStudent(Long studentId, UserPrincipal caller) {
        ownership.requireStudentAccess(caller, studentId);
        Student student = ownership.findStudentOrThrow(studentId);

        List<CourseGradeResponse> grades = gradeService.gradesForStudent(studentId, caller);

        // Group by (semester, academic year), preserving a sensible chronological order.
        Map<String, List<CourseGradeResponse>> bySemester = new LinkedHashMap<>();
        grades.stream()
                .sorted(Comparator.comparing(CourseGradeResponse::getAcademicYear)
                        .thenComparing(CourseGradeResponse::getSemester))
                .forEach(g -> bySemester
                        .computeIfAbsent(g.getAcademicYear() + "|" + g.getSemester(),
                                         k -> new ArrayList<>())
                        .add(g));

        List<TranscriptResponse.SemesterBlock> blocks = new ArrayList<>();

        BigDecimal cumulativePoints = BigDecimal.ZERO;
        int cumulativeGradedCredits = 0;
        int totalCredits = 0;
        int creditsEarned = 0;

        for (var entry : bySemester.entrySet()) {
            List<CourseGradeResponse> courses = entry.getValue();

            List<TranscriptLineResponse> lines = new ArrayList<>();
            BigDecimal semesterPoints = BigDecimal.ZERO;
            int semesterGradedCredits = 0;
            int semesterCredits = 0;

            for (CourseGradeResponse course : courses) {
                int credits = course.getCredits();
                semesterCredits += credits;
                totalCredits += credits;

                // Credits are "earned" only on a pass. An ungraded or failed course
                // contributes to the load but not to earned credits.
                if (Boolean.TRUE.equals(course.getPassed())) {
                    creditsEarned += credits;
                }

                if (course.getGradePoints() != null) {
                    semesterPoints = semesterPoints.add(
                            course.getGradePoints().multiply(BigDecimal.valueOf(credits)));
                    semesterGradedCredits += credits;
                    cumulativePoints = cumulativePoints.add(
                            course.getGradePoints().multiply(BigDecimal.valueOf(credits)));
                    cumulativeGradedCredits += credits;
                }

                lines.add(TranscriptLineResponse.builder()
                        .courseCode(course.getCourseCode())
                        .courseTitle(course.getCourseTitle())
                        .credits(credits)
                        .semester(course.getSemester())
                        .academicYear(course.getAcademicYear())
                        .percentage(course.getPercentage())
                        .grade(course.getGrade())
                        .gradePoints(course.getGradePoints())
                        .status(gradeStatus(course))
                        .build());
            }

            blocks.add(TranscriptResponse.SemesterBlock.builder()
                    .semester(courses.get(0).getSemester())
                    .academicYear(courses.get(0).getAcademicYear())
                    .courses(lines)
                    .semesterCredits(semesterCredits)
                    .semesterGpa(GradeUtil.gpa(semesterPoints, semesterGradedCredits))
                    .build());
        }

        return TranscriptResponse.builder()
                .studentId(student.getId())
                .studentName(student.getFullName())
                .rollNumber(student.getRollNumber())
                .admissionYear(student.getAdmissionYear())
                .departmentName(student.getDepartment().getName())
                .departmentCode(student.getDepartment().getCode())
                .semesters(blocks)
                .totalCredits(totalCredits)
                .creditsEarned(creditsEarned)
                .cumulativeGpa(GradeUtil.gpa(cumulativePoints, cumulativeGradedCredits))
                .build();
    }

    private String gradeStatus(CourseGradeResponse course) {
        if (course.getPassed() == null) return "IN_PROGRESS";
        return Boolean.TRUE.equals(course.getPassed()) ? "PASS" : "FAIL";
    }
}
