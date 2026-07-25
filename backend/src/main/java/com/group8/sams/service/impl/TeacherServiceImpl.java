package com.group8.sams.service.impl;

import com.group8.sams.dto.request.TeacherRequest;
import com.group8.sams.dto.response.PageResponse;
import com.group8.sams.dto.response.TeacherResponse;
import com.group8.sams.entity.*;
import com.group8.sams.exception.DuplicateResourceException;
import com.group8.sams.exception.ResourceNotFoundException;
import com.group8.sams.mapper.AcademicMapper;
import com.group8.sams.repository.*;
import com.group8.sams.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TeacherServiceImpl implements TeacherService {

    private static final Logger log = LoggerFactory.getLogger(TeacherServiceImpl.class);

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarkRepository markRepository;

    public TeacherServiceImpl(TeacherRepository teacherRepository,
                              UserRepository userRepository,
                              DepartmentRepository departmentRepository,
                              CourseRepository courseRepository,
                              AttendanceRepository attendanceRepository,
                              MarkRepository markRepository) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.courseRepository = courseRepository;
        this.attendanceRepository = attendanceRepository;
        this.markRepository = markRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TeacherResponse> findAll(Long departmentId, String search,
                                                 Pageable pageable) {
        String term = StringUtils.hasText(search) ? search : null;
        var page = teacherRepository.findByFilters(departmentId, term, pageable);
        return PageResponse.from(page, AcademicMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherResponse findById(Long id) {
        return AcademicMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherResponse findByUserId(Long userId) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No teacher profile is linked to this account"));
        return AcademicMapper.toResponse(teacher);
    }

    @Override
    @Transactional
    public TeacherResponse create(TeacherRequest request) {
        if (teacherRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new DuplicateResourceException(
                    "Teacher", "employee code", request.getEmployeeCode());
        }
        if (teacherRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateResourceException(
                    "A teacher profile already exists for user id " + request.getUserId());
        }

        Teacher teacher = Teacher.builder()
                .user(findUser(request.getUserId()))
                .department(findDepartment(request.getDepartmentId()))
                .employeeCode(request.getEmployeeCode())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .designation(request.getDesignation())
                .phone(request.getPhone())
                .build();

        return AcademicMapper.toResponse(teacherRepository.save(teacher));
    }

    @Override
    @Transactional
    public TeacherResponse update(Long id, TeacherRequest request) {
        Teacher teacher = getOrThrow(id);

        if (!teacher.getEmployeeCode().equals(request.getEmployeeCode())
                && teacherRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new DuplicateResourceException(
                    "Teacher", "employee code", request.getEmployeeCode());
        }

        teacher.setDepartment(findDepartment(request.getDepartmentId()));
        teacher.setEmployeeCode(request.getEmployeeCode());
        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setDesignation(request.getDesignation());
        teacher.setPhone(request.getPhone());

        return AcademicMapper.toResponse(teacherRepository.save(teacher));
    }

    /**
     * SET NULL semantics, implemented explicitly.
     *
     * A teacher is referenced from three places, and none of them should be
     * destroyed when the teacher leaves:
     *
     *   courses.teacher_id              -> the course survives, unassigned
     *   attendance.recorded_by_teacher  -> the attendance record survives, unattributed
     *   marks.entered_by_teacher        -> the mark survives, unattributed
     *
     * Losing attribution is acceptable; losing a student's attendance or grades
     * because a member of staff was removed is not.
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Teacher teacher = getOrThrow(id);

        List<Course> courses = courseRepository.findByTeacherId(id);
        courses.forEach(course -> course.setTeacher(null));
        courseRepository.saveAll(courses);

        List<Attendance> attendance = attendanceRepository.findByRecordedById(id);
        attendance.forEach(a -> a.setRecordedBy(null));
        attendanceRepository.saveAll(attendance);

        List<Mark> marks = markRepository.findByEnteredById(id);
        marks.forEach(m -> m.setEnteredBy(null));
        markRepository.saveAll(marks);

        teacherRepository.delete(teacher);

        log.info("Deleted teacher {}; unassigned {} course(s) and cleared attribution on {} attendance and {} mark record(s)",
                 teacher.getEmployeeCode(), courses.size(), attendance.size(), marks.size());
    }

    private Teacher getOrThrow(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", id));
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
