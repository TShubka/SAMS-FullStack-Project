package com.group8.sams.mapper;

import com.group8.sams.dto.response.*;
import com.group8.sams.entity.*;

/**
 * Entity to DTO conversion for the core academic records. Owner: Member 2.
 *
 * Every method here must be called inside a transactional service method: the
 * associations are LAZY, so touching course.getTeacher() outside a session would
 * throw LazyInitializationException.
 */
public final class AcademicMapper {

    private AcademicMapper() {
    }

    public static DepartmentResponse toResponse(Department d) {
        return DepartmentResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .code(d.getCode())
                .createdAt(d.getCreatedAt())
                .build();
    }

    public static StudentResponse toResponse(Student s) {
        return StudentResponse.builder()
                .id(s.getId())
                .rollNumber(s.getRollNumber())
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .fullName(s.getFullName())
                .admissionYear(s.getAdmissionYear())
                .currentSemester(s.getCurrentSemester())
                .phone(s.getPhone())
                .departmentId(s.getDepartment().getId())
                .departmentName(s.getDepartment().getName())
                .departmentCode(s.getDepartment().getCode())
                .userId(s.getUser().getId())
                .username(s.getUser().getUsername())
                .email(s.getUser().getEmail())
                .build();
    }

    public static TeacherResponse toResponse(Teacher t) {
        return TeacherResponse.builder()
                .id(t.getId())
                .employeeCode(t.getEmployeeCode())
                .firstName(t.getFirstName())
                .lastName(t.getLastName())
                .fullName(t.getFullName())
                .designation(t.getDesignation())
                .phone(t.getPhone())
                .departmentId(t.getDepartment().getId())
                .departmentName(t.getDepartment().getName())
                .userId(t.getUser().getId())
                .username(t.getUser().getUsername())
                .email(t.getUser().getEmail())
                .build();
    }

    public static CourseResponse toResponse(Course c) {
        Teacher teacher = c.getTeacher();
        return CourseResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .title(c.getTitle())
                .credits(c.getCredits())
                .semester(c.getSemester())
                .departmentId(c.getDepartment().getId())
                .departmentName(c.getDepartment().getName())
                .teacherId(teacher != null ? teacher.getId() : null)
                .teacherName(teacher != null ? teacher.getFullName() : null)
                .build();
    }

    public static EnrollmentResponse toResponse(Enrollment e) {
        Student s = e.getStudent();
        Course c = e.getCourse();
        return EnrollmentResponse.builder()
                .id(e.getId())
                .semester(e.getSemester())
                .academicYear(e.getAcademicYear())
                .status(e.getStatus().name())
                .enrolledOn(e.getEnrolledOn())
                .studentId(s.getId())
                .studentName(s.getFullName())
                .rollNumber(s.getRollNumber())
                .courseId(c.getId())
                .courseCode(c.getCode())
                .courseTitle(c.getTitle())
                .credits(c.getCredits())
                .build();
    }
}
