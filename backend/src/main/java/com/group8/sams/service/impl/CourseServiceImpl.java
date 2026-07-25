package com.group8.sams.service.impl;

import com.group8.sams.dto.request.CourseRequest;
import com.group8.sams.dto.response.CourseResponse;
import com.group8.sams.dto.response.PageResponse;
import com.group8.sams.entity.*;
import com.group8.sams.exception.DuplicateResourceException;
import com.group8.sams.exception.ResourceInUseException;
import com.group8.sams.exception.ResourceNotFoundException;
import com.group8.sams.mapper.AcademicMapper;
import com.group8.sams.repository.*;
import com.group8.sams.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseServiceImpl.class);

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final TeacherRepository teacherRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssessmentRepository assessmentRepository;

    public CourseServiceImpl(CourseRepository courseRepository,
                             DepartmentRepository departmentRepository,
                             TeacherRepository teacherRepository,
                             EnrollmentRepository enrollmentRepository,
                             AssessmentRepository assessmentRepository) {
        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
        this.teacherRepository = teacherRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.assessmentRepository = assessmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> findAll(Long departmentId, Integer semester,
                                                Long teacherId, String search,
                                                Pageable pageable) {
        String term = StringUtils.hasText(search) ? search : null;
        var page = courseRepository.findByFilters(departmentId, semester, teacherId, term, pageable);
        return PageResponse.from(page, AcademicMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        return AcademicMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> findMyCourses(Long userId) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No teacher profile is linked to this account"));
        return courseRepository.findByTeacherId(teacher.getId()).stream()
                .map(AcademicMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CourseResponse create(CourseRequest request) {
        String code = request.getCode().toUpperCase();
        if (courseRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Course", "code", code);
        }

        Course course = Course.builder()
                .code(code)
                .title(request.getTitle())
                .credits(request.getCredits())
                .semester(request.getSemester())
                .department(findDepartment(request.getDepartmentId()))
                .teacher(findTeacherOrNull(request.getTeacherId()))
                .build();

        return AcademicMapper.toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = getOrThrow(id);
        String code = request.getCode().toUpperCase();

        if (!course.getCode().equals(code) && courseRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Course", "code", code);
        }

        course.setCode(code);
        course.setTitle(request.getTitle());
        course.setCredits(request.getCredits());
        course.setSemester(request.getSemester());
        course.setDepartment(findDepartment(request.getDepartmentId()));
        // Passing a null teacherId unassigns the course, which is a legitimate action.
        course.setTeacher(findTeacherOrNull(request.getTeacherId()));

        return AcademicMapper.toResponse(courseRepository.save(course));
    }

    /**
     * RESTRICT semantics for enrollments, CASCADE for assessments.
     *
     * The asymmetry is deliberate. Enrollments belong to students, and deleting a
     * course with a roster would erase other people's academic history, so it is
     * refused. Assessments belong to the course itself and are meaningless without
     * it, so they are removed with it.
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Course course = getOrThrow(id);

        long enrolled = enrollmentRepository.countByCourseId(id);
        if (enrolled > 0) {
            throw new ResourceInUseException(
                    "Cannot delete course '%s': %d student(s) are enrolled. Remove the enrollments first."
                            .formatted(course.getCode(), enrolled));
        }

        List<Assessment> assessments = assessmentRepository.findByCourseId(id);
        assessmentRepository.deleteAll(assessments);
        courseRepository.delete(course);

        log.info("Deleted course {} and its {} assessment(s)",
                 course.getCode(), assessments.size());
    }

    private Course getOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
    }

    private Department findDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
    }

    private Teacher findTeacherOrNull(Long teacherId) {
        if (teacherId == null) return null;
        return teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
    }
}
