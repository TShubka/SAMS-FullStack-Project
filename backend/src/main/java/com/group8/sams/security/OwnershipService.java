package com.group8.sams.security;

import com.group8.sams.entity.Course;
import com.group8.sams.entity.Student;
import com.group8.sams.entity.Teacher;
import com.group8.sams.exception.ResourceNotFoundException;
import com.group8.sams.exception.UnauthorizedActionException;
import com.group8.sams.repository.StudentRepository;
import com.group8.sams.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fine-grained ownership checks. Owner: Member 1, used by Members 3 and 4.
 *
 * Roles alone are not enough. @PreAuthorize can say "a teacher may record
 * attendance", but only a query can answer "is this teacher assigned to THIS
 * course?" - so that half of the rule lives here, in the service layer, where the
 * record can actually be loaded and compared against the caller.
 *
 * Everything refused here returns 403, never 404: the resource exists, the caller
 * simply may not have it. Returning 404 would leak nothing but would also mislead
 * legitimate users debugging their own access.
 */
@Service
public class OwnershipService {

    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public OwnershipService(TeacherRepository teacherRepository,
                            StudentRepository studentRepository) {
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    /** The teacher profile attached to the calling account. */
    @Transactional(readOnly = true)
    public Teacher requireTeacher(UserPrincipal principal) {
        return teacherRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new UnauthorizedActionException(
                        "This account is not linked to a teacher profile"));
    }

    /** The student profile attached to the calling account. */
    @Transactional(readOnly = true)
    public Student requireStudent(UserPrincipal principal) {
        return studentRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new UnauthorizedActionException(
                        "This account is not linked to a student profile"));
    }

    public boolean isAdmin(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    public boolean isTeacher(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_TEACHER".equals(a.getAuthority()));
    }

    public boolean isStudent(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_STUDENT".equals(a.getAuthority()));
    }

    /**
     * Admins pass. A teacher passes only for a course they are assigned to.
     * An unassigned course (teacher is null) is writable by admins only - there is
     * no owner to authorize.
     */
    @Transactional(readOnly = true)
    public void requireCourseAccess(UserPrincipal principal, Course course) {
        if (isAdmin(principal)) return;

        Teacher teacher = requireTeacher(principal);
        Teacher assigned = course.getTeacher();

        if (assigned == null) {
            throw new UnauthorizedActionException(
                    "Course '%s' has no assigned teacher. Only an administrator may modify it."
                            .formatted(course.getCode()));
        }
        if (!assigned.getId().equals(teacher.getId())) {
            throw new UnauthorizedActionException(
                    "You are not the teacher assigned to course '%s'".formatted(course.getCode()));
        }
    }

    /**
     * Admins and teachers pass. A student passes only for their own record.
     * This is the rule that stops a student reading a classmate's attendance,
     * marks or transcript by changing an id in the URL.
     */
    @Transactional(readOnly = true)
    public void requireStudentAccess(UserPrincipal principal, Long studentId) {
        if (isAdmin(principal) || isTeacher(principal)) return;

        Student student = requireStudent(principal);
        if (!student.getId().equals(studentId)) {
            throw new UnauthorizedActionException(
                    "You may only view your own records");
        }
    }

    @Transactional(readOnly = true)
    public Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
    }
}
