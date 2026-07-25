package com.group8.sams.service.impl;

import com.group8.sams.dto.request.StudentRequest;
import com.group8.sams.dto.response.PageResponse;
import com.group8.sams.dto.response.StudentResponse;
import com.group8.sams.entity.*;
import com.group8.sams.exception.DuplicateResourceException;
import com.group8.sams.exception.ResourceNotFoundException;
import com.group8.sams.mapper.AcademicMapper;
import com.group8.sams.repository.*;
import com.group8.sams.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarkRepository markRepository;

    public StudentServiceImpl(StudentRepository studentRepository,
                              UserRepository userRepository,
                              DepartmentRepository departmentRepository,
                              EnrollmentRepository enrollmentRepository,
                              AttendanceRepository attendanceRepository,
                              MarkRepository markRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.markRepository = markRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> findAll(Long departmentId, Integer admissionYear,
                                                 String search, Pageable pageable) {
        String term = StringUtils.hasText(search) ? search : null;
        var page = studentRepository.findByFilters(departmentId, admissionYear, term, pageable);
        return PageResponse.from(page, AcademicMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        return AcademicMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse findByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No student profile is linked to this account"));
        return AcademicMapper.toResponse(student);
    }

    @Override
    @Transactional
    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByRollNumber(request.getRollNumber())) {
            throw new DuplicateResourceException("Student", "roll number", request.getRollNumber());
        }
        // The 1:1 with User is enforced by a unique constraint; pre-checking turns
        // that into a readable message instead of a raw constraint violation.
        if (studentRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateResourceException(
                    "A student profile already exists for user id " + request.getUserId());
        }

        Student student = Student.builder()
                .user(findUser(request.getUserId()))
                .department(findDepartment(request.getDepartmentId()))
                .rollNumber(request.getRollNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .admissionYear(request.getAdmissionYear())
                .currentSemester(request.getCurrentSemester())
                .phone(request.getPhone())
                .build();

        return AcademicMapper.toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = getOrThrow(id);

        if (!student.getRollNumber().equals(request.getRollNumber())
                && studentRepository.existsByRollNumber(request.getRollNumber())) {
            throw new DuplicateResourceException("Student", "roll number", request.getRollNumber());
        }

        // The linked user account is deliberately not reassignable: moving a profile
        // between accounts would silently transfer someone's academic record.
        student.setDepartment(findDepartment(request.getDepartmentId()));
        student.setRollNumber(request.getRollNumber());
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setAdmissionYear(request.getAdmissionYear());
        student.setCurrentSemester(request.getCurrentSemester());
        student.setPhone(request.getPhone());

        return AcademicMapper.toResponse(studentRepository.save(student));
    }

    /**
     * CASCADE semantics, implemented explicitly.
     *
     * The database foreign keys are NO ACTION because Hibernate does not generate
     * ON DELETE clauses, so the cascade from the Phase 1 design is performed here in
     * child-to-parent order: marks and attendance hang off enrollments, enrollments
     * hang off the student. Deleting in any other order would violate a foreign key.
     *
     * The linked User account is intentionally left in place - it is a login
     * identity, not academic data, and removing it is a separate administrative act.
     *
     * The whole method is one transaction, so a failure part-way through rolls the
     * entire deletion back rather than leaving orphaned rows.
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Student student = getOrThrow(id);

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(id);

        for (Enrollment enrollment : enrollments) {
            markRepository.deleteAll(markRepository.findByEnrollmentId(enrollment.getId()));
            attendanceRepository.deleteAll(
                    attendanceRepository.findByEnrollmentId(enrollment.getId()));
        }
        enrollmentRepository.deleteAll(enrollments);
        studentRepository.delete(student);

        log.info("Deleted student {} together with {} enrollment(s) and their attendance and marks",
                 student.getRollNumber(), enrollments.size());
    }

    private Student getOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private Department findDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
    }
}
