package com.group8.sams.service.impl;

import com.group8.sams.dto.request.DepartmentRequest;
import com.group8.sams.dto.response.DepartmentResponse;
import com.group8.sams.dto.response.PageResponse;
import com.group8.sams.entity.Department;
import com.group8.sams.exception.DuplicateResourceException;
import com.group8.sams.exception.ResourceInUseException;
import com.group8.sams.exception.ResourceNotFoundException;
import com.group8.sams.mapper.AcademicMapper;
import com.group8.sams.repository.*;
import com.group8.sams.service.DepartmentService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 StudentRepository studentRepository,
                                 TeacherRepository teacherRepository,
                                 CourseRepository courseRepository) {
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> findAll(String search, Pageable pageable) {
        var page = StringUtils.hasText(search)
                ? departmentRepository.search(search, pageable)
                : departmentRepository.findAll(pageable);
        return PageResponse.from(page, AcademicMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        return AcademicMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        String code = request.getCode().toUpperCase();

        if (departmentRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Department", "code", code);
        }
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department", "name", request.getName());
        }

        Department department = Department.builder()
                .name(request.getName())
                .code(code)
                .build();

        return AcademicMapper.toResponse(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = getOrThrow(id);
        String code = request.getCode().toUpperCase();

        // Only complain about a duplicate when the value actually changed, otherwise
        // saving a record without editing its code would report a conflict with itself.
        if (!department.getCode().equals(code) && departmentRepository.existsByCode(code)) {
            throw new DuplicateResourceException("Department", "code", code);
        }
        if (!department.getName().equals(request.getName())
                && departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department", "name", request.getName());
        }

        department.setName(request.getName());
        department.setCode(code);

        return AcademicMapper.toResponse(departmentRepository.save(department));
    }

    /**
     * RESTRICT semantics, enforced here rather than by the database.
     *
     * Hibernate's schema generation does not emit ON DELETE clauses, so the
     * referential rules from the design live in the services. Deleting a department
     * that still has students, teachers or courses would silently orphan academic
     * history, so it is refused with a 409 that names what is blocking it.
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Department department = getOrThrow(id);

        long students = studentRepository.countByDepartmentId(id);
        long teachers = teacherRepository.countByDepartmentId(id);
        long courses = courseRepository.countByDepartmentId(id);

        if (students > 0 || teachers > 0 || courses > 0) {
            throw new ResourceInUseException(
                    "Cannot delete department '%s': it still has %d student(s), %d teacher(s) and %d course(s). Reassign or remove them first."
                            .formatted(department.getCode(), students, teachers, courses));
        }

        departmentRepository.delete(department);
    }

    private Department getOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }
}
