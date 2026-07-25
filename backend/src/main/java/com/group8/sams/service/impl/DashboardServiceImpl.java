package com.group8.sams.service.impl;

import com.group8.sams.dto.response.CourseGradeResponse;
import com.group8.sams.dto.response.CourseResponse;
import com.group8.sams.dto.response.dashboard.*;
import com.group8.sams.dto.response.report.StudentsByDepartmentReport;
import com.group8.sams.entity.Student;
import com.group8.sams.entity.Teacher;
import com.group8.sams.mapper.AcademicMapper;
import com.group8.sams.repository.*;
import com.group8.sams.security.OwnershipService;
import com.group8.sams.security.UserPrincipal;
import com.group8.sams.service.AttendanceService;
import com.group8.sams.service.DashboardService;
import com.group8.sams.service.GradeService;
import com.group8.sams.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Role-based dashboards. Owner: Member 4.
 *
 * Each dashboard reuses the report and grade services rather than re-querying, so
 * the numbers shown on a dashboard always match the corresponding report. Every
 * value is computed from the database on request - nothing here is sample data.
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ReportService reportService;
    private final GradeService gradeService;
    private final AttendanceService attendanceService;
    private final OwnershipService ownership;

    public DashboardServiceImpl(StudentRepository studentRepository,
                                TeacherRepository teacherRepository,
                                DepartmentRepository departmentRepository,
                                CourseRepository courseRepository,
                                EnrollmentRepository enrollmentRepository,
                                ReportService reportService,
                                GradeService gradeService,
                                AttendanceService attendanceService,
                                OwnershipService ownership) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.reportService = reportService;
        this.gradeService = gradeService;
        this.attendanceService = attendanceService;
        this.ownership = ownership;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse admin(UserPrincipal caller) {
        StudentsByDepartmentReport byDept = reportService.studentsByDepartment();

        return AdminDashboardResponse.builder()
                .totalStudents(studentRepository.count())
                .totalTeachers(teacherRepository.count())
                .totalDepartments(departmentRepository.count())
                .totalCourses(courseRepository.count())
                .totalEnrollments(enrollmentRepository.count())
                .studentsByDepartment(byDept.getDepartments())
                .lowAttendanceCount(attendanceService.lowAttendance(null, caller).size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherDashboardResponse teacher(UserPrincipal caller) {
        Teacher teacher = ownership.requireTeacher(caller);

        List<CourseResponse> courses = courseRepository.findByTeacherId(teacher.getId()).stream()
                .map(AcademicMapper::toResponse)
                .toList();

        long students = courseRepository.findByTeacherId(teacher.getId()).stream()
                .mapToLong(c -> enrollmentRepository.countByCourseId(c.getId()))
                .sum();

        long lowAttendance = attendanceService.lowAttendance(null, caller).size();

        return TeacherDashboardResponse.builder()
                .teacherName(teacher.getFullName())
                .employeeCode(teacher.getEmployeeCode())
                .departmentName(teacher.getDepartment().getName())
                .assignedCourses(courses.size())
                .totalStudents(students)
                .lowAttendanceStudents(lowAttendance)
                .courses(courses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDashboardResponse student(UserPrincipal caller) {
        Student student = ownership.requireStudent(caller);

        List<CourseGradeResponse> grades =
                gradeService.gradesForStudent(student.getId(), caller);

        long passed = grades.stream().filter(c -> Boolean.TRUE.equals(c.getPassed())).count();

        var gpa = gradeService.gpa(student.getId(), null, null, caller);

        // Real count of the student's own courses below the attendance threshold.
        long lowAttendance = grades.stream()
                .map(g -> attendanceService.percentage(student.getId(), g.getCourseId(), caller))
                .filter(s -> s != null && s.isBelowThreshold())
                .count();

        return StudentDashboardResponse.builder()
                .studentName(student.getFullName())
                .rollNumber(student.getRollNumber())
                .departmentName(student.getDepartment().getName())
                .currentSemester(student.getCurrentSemester())
                .enrolledCourses(grades.size())
                .cumulativeGpa(gpa.getGpa())
                .coursesPassed(passed)
                .lowAttendanceCourses(lowAttendance)
                .courses(grades)
                .build();
    }
}
