package com.group6.sams.service.impl;

import com.group6.sams.dto.response.AttendanceSummaryResponse;
import com.group6.sams.dto.response.CourseGradeResponse;
import com.group6.sams.dto.response.report.*;
import com.group6.sams.entity.Course;
import com.group6.sams.entity.Department;
import com.group6.sams.entity.Student;
import com.group6.sams.exception.ResourceNotFoundException;
import com.group6.sams.repository.*;
import com.group6.sams.security.OwnershipService;
import com.group6.sams.security.UserPrincipal;
import com.group6.sams.service.AttendanceService;
import com.group6.sams.service.GradeService;
import com.group6.sams.service.ReportService;
import com.group6.sams.util.GradeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Academic and department reports. Owner: Member 4.
 *
 * The grade-based reports reuse GradeService rather than re-deriving grades, so
 * every report agrees with the marks page and the transcript. Simple counts are
 * aggregated in the database via projections; grade bucketing is done in Java by
 * GradeUtil, deliberately, to keep the grade boundaries in one place instead of
 * duplicating them into SQL CASE expressions.
 */
@Service
public class ReportServiceImpl implements ReportService {

    /** Letter grades in scale order, so a distribution is always fully zero-filled. */
    private static final List<String> GRADE_ORDER =
            List.of("A+", "A", "B", "C", "D", "F");

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeService gradeService;
    private final AttendanceService attendanceService;
    private final OwnershipService ownership;

    public ReportServiceImpl(StudentRepository studentRepository,
                             CourseRepository courseRepository,
                             DepartmentRepository departmentRepository,
                             EnrollmentRepository enrollmentRepository,
                             GradeService gradeService,
                             AttendanceService attendanceService,
                             OwnershipService ownership) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.gradeService = gradeService;
        this.attendanceService = attendanceService;
        this.ownership = ownership;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentsByDepartmentReport studentsByDepartment() {
        List<StudentsByDepartmentReport.Row> rows =
                studentRepository.countStudentsByDepartment().stream()
                        .map(p -> StudentsByDepartmentReport.Row.builder()
                                .departmentId(p.getDepartmentId())
                                .departmentName(p.getDepartmentName())
                                .departmentCode(p.getDepartmentCode())
                                .studentCount(p.getStudentCount())
                                .build())
                        .toList();

        long total = rows.stream().mapToLong(StudentsByDepartmentReport.Row::getStudentCount).sum();
        return StudentsByDepartmentReport.builder()
                .totalStudents(total)
                .departments(rows)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentPerformanceReport studentPerformance(Long studentId, UserPrincipal caller) {
        ownership.requireStudentAccess(caller, studentId);
        Student student = ownership.findStudentOrThrow(studentId);

        List<CourseGradeResponse> courses = gradeService.gradesForStudent(studentId, caller);

        long passed = courses.stream().filter(c -> Boolean.TRUE.equals(c.getPassed())).count();
        long failed = courses.stream().filter(c -> Boolean.FALSE.equals(c.getPassed())).count();
        int graded = (int) courses.stream().filter(c -> c.getGradePoints() != null).count();

        return StudentPerformanceReport.builder()
                .studentId(student.getId())
                .studentName(student.getFullName())
                .rollNumber(student.getRollNumber())
                .departmentName(student.getDepartment().getName())
                .cumulativeGpa(gradeService.gpa(studentId, null, null, caller).getGpa())
                .totalCourses(courses.size())
                .gradedCourses(graded)
                .passed(passed)
                .failed(failed)
                .courses(courses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceSummaryResponse> attendanceByCourse(Long courseId,
                                                              UserPrincipal caller) {
        // Reuses the attendance service, which applies the owning-teacher check.
        return attendanceService.courseSummary(courseId, caller);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceSummaryResponse> lowAttendance(BigDecimal threshold,
                                                        UserPrincipal caller) {
        return attendanceService.lowAttendance(threshold, caller);
    }

    @Override
    @Transactional(readOnly = true)
    public CoursePerformanceReport coursePerformance(Long courseId, UserPrincipal caller) {
        Course course = findCourse(courseId);
        ownership.requireCourseAccess(caller, course);

        List<CourseGradeResponse> grades = enrollmentRepository.findByCourseId(courseId).stream()
                .map(e -> gradeService.gradeForEnrollment(e.getId(), caller))
                .toList();

        List<BigDecimal> pcts = grades.stream()
                .map(CourseGradeResponse::getPercentage)
                .filter(Objects::nonNull)
                .toList();

        return CoursePerformanceReport.builder()
                .courseId(course.getId())
                .courseCode(course.getCode())
                .courseTitle(course.getTitle())
                .enrolled(grades.size())
                .graded(pcts.size())
                .averagePercentage(average(pcts))
                .highestPercentage(pcts.stream().max(BigDecimal::compareTo).orElse(null))
                .lowestPercentage(pcts.stream().min(BigDecimal::compareTo).orElse(null))
                .students(grades)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GradeDistributionReport gradeDistribution(Long courseId, UserPrincipal caller) {
        Course course = findCourse(courseId);
        ownership.requireCourseAccess(caller, course);

        List<CourseGradeResponse> grades = enrollmentRepository.findByCourseId(courseId).stream()
                .map(e -> gradeService.gradeForEnrollment(e.getId(), caller))
                .toList();

        // Zero-fill every grade so the chart has a bar for each, even at zero.
        Map<String, Long> counts = new LinkedHashMap<>();
        GRADE_ORDER.forEach(g -> counts.put(g, 0L));

        long ungraded = 0;
        for (CourseGradeResponse g : grades) {
            if (g.getGrade() == null) {
                ungraded++;
            } else {
                counts.merge(g.getGrade(), 1L, Long::sum);
            }
        }

        List<GradeDistributionReport.Bucket> buckets = GRADE_ORDER.stream()
                .map(g -> GradeDistributionReport.Bucket.builder()
                        .grade(g).count(counts.get(g)).build())
                .toList();

        return GradeDistributionReport.builder()
                .courseId(course.getId())
                .courseCode(course.getCode())
                .courseTitle(course.getTitle())
                .gradedStudents(grades.size() - ungraded)
                .ungradedStudents(ungraded)
                .distribution(buckets)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PassFailReport passFail(Long courseId, UserPrincipal caller) {
        Course course = findCourse(courseId);
        ownership.requireCourseAccess(caller, course);

        List<CourseGradeResponse> grades = enrollmentRepository.findByCourseId(courseId).stream()
                .map(e -> gradeService.gradeForEnrollment(e.getId(), caller))
                .toList();

        long passed = grades.stream().filter(c -> Boolean.TRUE.equals(c.getPassed())).count();
        long failed = grades.stream().filter(c -> Boolean.FALSE.equals(c.getPassed())).count();
        long graded = passed + failed;

        BigDecimal passRate = graded == 0 ? null
                : BigDecimal.valueOf(passed).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(graded), 2, RoundingMode.HALF_UP);

        return PassFailReport.builder()
                .courseId(course.getId())
                .courseCode(course.getCode())
                .courseTitle(course.getTitle())
                .graded(graded)
                .passed(passed)
                .failed(failed)
                .passRate(passRate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentPerformanceReport departmentPerformance(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

        List<Student> students = studentRepository.findByDepartmentId(departmentId);

        // Admin-only report, so a system principal is fine here - the endpoint is
        // already restricted to ADMIN and every student in the department is in scope.
        List<BigDecimal> gpas = students.stream()
                .map(s -> gradeService.gpa(s.getId(), null, null, adminView()).getGpa())
                .filter(Objects::nonNull)
                .toList();

        return DepartmentPerformanceReport.builder()
                .departmentId(department.getId())
                .departmentName(department.getName())
                .departmentCode(department.getCode())
                .totalStudents(students.size())
                .studentsWithGpa(gpas.size())
                .averageGpa(average(gpas))
                .highestGpa(gpas.stream().max(BigDecimal::compareTo).orElse(null))
                .lowestGpa(gpas.stream().min(BigDecimal::compareTo).orElse(null))
                .totalCourses(courseRepository.countByDepartmentId(departmentId))
                .build();
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) return null;
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
    }

    /**
     * A synthetic ADMIN principal used only for the department report, which is
     * itself ADMIN-restricted at the controller. It lets the report reuse
     * GradeService's student-scoped methods without each student being the caller.
     */
    private UserPrincipal adminView() {
        return new UserPrincipal(-1L, "system", "system", "",
                true, List.of(new org.springframework.security.core.authority
                        .SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
