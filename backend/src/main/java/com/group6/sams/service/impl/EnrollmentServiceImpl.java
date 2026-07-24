package com.group6.sams.service.impl;

import com.group6.sams.dto.request.EnrollmentRequest;
import com.group6.sams.dto.response.EnrollmentResponse;
import com.group6.sams.dto.response.PageResponse;
import com.group6.sams.entity.*;
import com.group6.sams.entity.enums.EnrollmentStatus;
import com.group6.sams.exception.DuplicateResourceException;
import com.group6.sams.exception.ResourceNotFoundException;
import com.group6.sams.mapper.AcademicMapper;
import com.group6.sams.repository.*;
import com.group6.sams.service.EnrollmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentServiceImpl.class);

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarkRepository markRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,
                                 StudentRepository studentRepository,
                                 CourseRepository courseRepository,
                                 AttendanceRepository attendanceRepository,
                                 MarkRepository markRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.attendanceRepository = attendanceRepository;
        this.markRepository = markRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EnrollmentResponse> findAll(Pageable pageable) {
        return PageResponse.from(enrollmentRepository.findAll(pageable),
                                 AcademicMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse findById(Long id) {
        return AcademicMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(AcademicMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(AcademicMapper::toResponse)
                .toList();
    }

    /**
     * Duplicate enrollment is prevented in two layers: this pre-check produces a
     * readable 409, and the composite unique constraint on the table is the real
     * guarantee if two requests race past the check concurrently.
     */
    @Override
    @Transactional
    public EnrollmentResponse create(EnrollmentRequest request) {
        if (enrollmentRepository.existsByStudentIdAndCourseIdAndSemesterAndAcademicYear(
                request.getStudentId(), request.getCourseId(),
                request.getSemester(), request.getAcademicYear())) {
            throw new DuplicateResourceException(
                    "This student is already enrolled in this course for semester %d of %s"
                            .formatted(request.getSemester(), request.getAcademicYear()));
        }

        Enrollment enrollment = Enrollment.builder()
                .student(findStudent(request.getStudentId()))
                .course(findCourse(request.getCourseId()))
                .semester(request.getSemester())
                .academicYear(request.getAcademicYear())
                .status(resolveStatus(request.getStatus()))
                .build();

        return AcademicMapper.toResponse(enrollmentRepository.save(enrollment));
    }

    /**
     * Only the status is editable. Changing the student, course, semester or
     * academic year would turn this into a different enrollment entirely while
     * keeping its attendance and marks attached - a silent data-integrity failure.
     * Callers who need that must delete and re-create.
     */
    @Override
    @Transactional
    public EnrollmentResponse update(Long id, EnrollmentRequest request) {
        Enrollment enrollment = getOrThrow(id);
        enrollment.setStatus(resolveStatus(request.getStatus()));
        return AcademicMapper.toResponse(enrollmentRepository.save(enrollment));
    }

    /**
     * CASCADE semantics: attendance and marks belong wholly to this enrollment and
     * have no meaning without it, so they are removed first (child before parent, or
     * the foreign keys would reject the delete).
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Enrollment enrollment = getOrThrow(id);

        List<Mark> marks = markRepository.findByEnrollmentId(id);
        List<Attendance> attendance = attendanceRepository.findByEnrollmentId(id);

        markRepository.deleteAll(marks);
        attendanceRepository.deleteAll(attendance);
        enrollmentRepository.delete(enrollment);

        log.info("Deleted enrollment {} with {} mark(s) and {} attendance record(s)",
                 id, marks.size(), attendance.size());
    }

    private Enrollment getOrThrow(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
    }

    private EnrollmentStatus resolveStatus(String status) {
        return (status == null || status.isBlank())
                ? EnrollmentStatus.ACTIVE
                : EnrollmentStatus.valueOf(status);
    }
}
