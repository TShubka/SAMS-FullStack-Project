package com.group6.sams.service.impl;

import com.group6.sams.dto.response.CourseGradeResponse;
import com.group6.sams.dto.response.GpaResponse;
import com.group6.sams.dto.response.MarkResponse;
import com.group6.sams.entity.*;
import com.group6.sams.exception.ResourceNotFoundException;
import com.group6.sams.mapper.GradeMapper;
import com.group6.sams.repository.EnrollmentRepository;
import com.group6.sams.repository.MarkRepository;
import com.group6.sams.security.OwnershipService;
import com.group6.sams.security.UserPrincipal;
import com.group6.sams.service.GradeService;
import com.group6.sams.util.GradeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Grade and GPA computation. Owner: Member 3, consumed by Member 4's transcript
 * and reports.
 *
 * Nothing here is persisted. Grades are derived from marks every time they are
 * asked for, which is why a corrected mark immediately produces a corrected grade
 * and GPA with no reconciliation step.
 *
 * All scale decisions are delegated to GradeUtil so that the transcript, the
 * reports and this service can never disagree.
 */
@Service
public class GradeServiceImpl implements GradeService {

    private final EnrollmentRepository enrollmentRepository;
    private final MarkRepository markRepository;
    private final OwnershipService ownership;

    public GradeServiceImpl(EnrollmentRepository enrollmentRepository,
                            MarkRepository markRepository,
                            OwnershipService ownership) {
        this.enrollmentRepository = enrollmentRepository;
        this.markRepository = markRepository;
        this.ownership = ownership;
    }

    @Override
    @Transactional(readOnly = true)
    public CourseGradeResponse gradeForEnrollment(Long enrollmentId, UserPrincipal caller) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment", "id", enrollmentId));
        ownership.requireStudentAccess(caller, enrollment.getStudent().getId());
        return computeGrade(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseGradeResponse> gradesForStudent(Long studentId, UserPrincipal caller) {
        ownership.requireStudentAccess(caller, studentId);
        ownership.findStudentOrThrow(studentId);

        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::computeGrade)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GpaResponse gpa(Long studentId, Integer semester, String academicYear,
                           UserPrincipal caller) {
        ownership.requireStudentAccess(caller, studentId);
        Student student = ownership.findStudentOrThrow(studentId);

        List<Enrollment> enrollments =
                (semester != null && academicYear != null)
                        ? enrollmentRepository.findByStudentIdAndSemesterAndAcademicYear(
                                studentId, semester, academicYear)
                        : enrollmentRepository.findByStudentId(studentId);

        List<CourseGradeResponse> courses = enrollments.stream()
                .map(this::computeGrade)
                .toList();

        BigDecimal weightedPoints = BigDecimal.ZERO;
        int totalCredits = 0;
        int graded = 0;
        int ungraded = 0;

        for (CourseGradeResponse course : courses) {
            // Ungraded courses contribute neither points nor credits. Counting their
            // credits with zero points would drag the GPA down for work that has
            // simply not been marked yet.
            if (course.getGradePoints() == null) {
                ungraded++;
                continue;
            }
            graded++;
            int credits = course.getCredits();
            totalCredits += credits;
            weightedPoints = weightedPoints.add(
                    course.getGradePoints().multiply(BigDecimal.valueOf(credits)));
        }

        return GpaResponse.builder()
                .studentId(student.getId())
                .studentName(student.getFullName())
                .rollNumber(student.getRollNumber())
                .semester(semester)
                .academicYear(academicYear)
                .gpa(GradeUtil.gpa(weightedPoints, totalCredits))
                .totalCredits(totalCredits)
                .gradedCourses(graded)
                .ungradedCourses(ungraded)
                .courses(courses)
                .build();
    }

    /**
     * Weighted result for one enrollment.
     *
     * Each mark contributes (obtained / max) * weight, and the divisor is the weight
     * actually recorded rather than a fixed 100 - so a student who has sat only the
     * midterm sees a meaningful figure instead of an artificially low one.
     */
    private CourseGradeResponse computeGrade(Enrollment enrollment) {
        List<Mark> marks = markRepository.findByEnrollmentId(enrollment.getId());

        BigDecimal weightedScore = BigDecimal.ZERO;
        BigDecimal recordedWeight = BigDecimal.ZERO;

        for (Mark mark : marks) {
            Assessment assessment = mark.getAssessment();
            BigDecimal max = assessment.getMaxMarks();
            BigDecimal weight = assessment.getWeightPercent();

            if (max == null || max.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal ratio = mark.getMarksObtained()
                    .divide(max, 6, RoundingMode.HALF_UP);
            weightedScore = weightedScore.add(ratio.multiply(weight));
            recordedWeight = recordedWeight.add(weight);
        }

        BigDecimal percentage = GradeUtil.weightedPercentage(weightedScore, recordedWeight);

        Student student = enrollment.getStudent();
        Course course = enrollment.getCourse();

        List<MarkResponse> markResponses = marks.stream()
                .map(GradeMapper::toResponse)
                .toList();

        return CourseGradeResponse.builder()
                .enrollmentId(enrollment.getId())
                .studentId(student.getId())
                .studentName(student.getFullName())
                .rollNumber(student.getRollNumber())
                .courseId(course.getId())
                .courseCode(course.getCode())
                .courseTitle(course.getTitle())
                .credits(course.getCredits())
                .semester(enrollment.getSemester())
                .academicYear(enrollment.getAcademicYear())
                .weightedScore(weightedScore.setScale(2, RoundingMode.HALF_UP))
                .recordedWeight(recordedWeight.setScale(2, RoundingMode.HALF_UP))
                .percentage(percentage)
                .grade(GradeUtil.letterGrade(percentage))
                .gradePoints(GradeUtil.gradePoints(percentage))
                .passed(GradeUtil.isPass(percentage))
                .marks(markResponses)
                .build();
    }
}
