package com.group6.sams.service.impl;

import com.group6.sams.dto.request.MarkRequest;
import com.group6.sams.dto.response.MarkResponse;
import com.group6.sams.entity.*;
import com.group6.sams.exception.BusinessRuleException;
import com.group6.sams.exception.DuplicateResourceException;
import com.group6.sams.exception.ResourceNotFoundException;
import com.group6.sams.mapper.GradeMapper;
import com.group6.sams.repository.*;
import com.group6.sams.security.OwnershipService;
import com.group6.sams.security.UserPrincipal;
import com.group6.sams.service.MarkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Mark entry. Owner: Member 3.
 *
 * Three integrity rules live here because no single-row constraint can express
 * them:
 *
 *   1. marksObtained must not exceed the parent assessment's maxMarks. A CHECK
 *      constraint cannot see the parent row. (The >= 0 half IS in the database.)
 *   2. The enrollment and the assessment must belong to the same course, otherwise
 *      a Physics quiz score could be filed against a Chemistry enrollment.
 *   3. One mark per (enrollment, assessment). The unique constraint is the real
 *      guarantee; the pre-check turns it into a readable 409.
 *
 * Students never reach this class: SecurityConfig refuses ROLE_STUDENT on every
 * mark write, and the owning-teacher check below refuses everyone else.
 */
@Service
public class MarkServiceImpl implements MarkService {

    private static final Logger log = LoggerFactory.getLogger(MarkServiceImpl.class);

    private final MarkRepository markRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssessmentRepository assessmentRepository;
    private final CourseRepository courseRepository;
    private final OwnershipService ownership;

    public MarkServiceImpl(MarkRepository markRepository,
                           EnrollmentRepository enrollmentRepository,
                           AssessmentRepository assessmentRepository,
                           CourseRepository courseRepository,
                           OwnershipService ownership) {
        this.markRepository = markRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.assessmentRepository = assessmentRepository;
        this.courseRepository = courseRepository;
        this.ownership = ownership;
    }

    @Override
    @Transactional
    public MarkResponse create(MarkRequest request, UserPrincipal caller) {
        Enrollment enrollment = findEnrollment(request.getEnrollmentId());
        Assessment assessment = findAssessment(request.getAssessmentId());

        ownership.requireCourseAccess(caller, enrollment.getCourse());
        requireSameCourse(enrollment, assessment);
        requireWithinMaxMarks(request, assessment);

        if (markRepository.existsByEnrollmentIdAndAssessmentId(
                enrollment.getId(), assessment.getId())) {
            throw new DuplicateResourceException(
                    "A mark for %s on '%s' already exists. Use PUT to change it."
                            .formatted(enrollment.getStudent().getRollNumber(),
                                       assessment.getTitle()));
        }

        Mark mark = Mark.builder()
                .enrollment(enrollment)
                .assessment(assessment)
                .marksObtained(request.getMarksObtained())
                .enteredBy(resolveEnteredBy(caller))
                .build();

        return GradeMapper.toResponse(markRepository.save(mark));
    }

    /**
     * Only the score is editable. Moving a mark to a different enrollment or
     * assessment would collide with the unique constraint or silently reassign one
     * student's score to another.
     */
    @Override
    @Transactional
    public MarkResponse update(Long id, MarkRequest request, UserPrincipal caller) {
        Mark mark = getOrThrow(id);
        ownership.requireCourseAccess(caller, mark.getEnrollment().getCourse());
        requireWithinMaxMarks(request, mark.getAssessment());

        mark.setMarksObtained(request.getMarksObtained());
        mark.setEnteredBy(resolveEnteredBy(caller));

        return GradeMapper.toResponse(markRepository.save(mark));
    }

    @Override
    @Transactional
    public void delete(Long id, UserPrincipal caller) {
        Mark mark = getOrThrow(id);
        ownership.requireCourseAccess(caller, mark.getEnrollment().getCourse());
        markRepository.delete(mark);
        log.info("Deleted mark {} for {}", id, mark.getEnrollment().getStudent().getRollNumber());
    }

    /** A student may read the marks on their own enrollment; anyone else gets 403. */
    @Override
    @Transactional(readOnly = true)
    public List<MarkResponse> findByEnrollment(Long enrollmentId, UserPrincipal caller) {
        Enrollment enrollment = findEnrollment(enrollmentId);
        ownership.requireStudentAccess(caller, enrollment.getStudent().getId());

        return markRepository.findByEnrollmentId(enrollmentId).stream()
                .map(GradeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarkResponse> findByCourse(Long courseId, UserPrincipal caller) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        ownership.requireCourseAccess(caller, course);

        return markRepository.findByEnrollmentCourseId(courseId).stream()
                .map(GradeMapper::toResponse)
                .toList();
    }

    private void requireSameCourse(Enrollment enrollment, Assessment assessment) {
        Long enrollmentCourse = enrollment.getCourse().getId();
        Long assessmentCourse = assessment.getCourse().getId();

        if (!enrollmentCourse.equals(assessmentCourse)) {
            throw new BusinessRuleException(
                    "Assessment '%s' belongs to course '%s', but the enrollment is for course '%s'"
                            .formatted(assessment.getTitle(),
                                       assessment.getCourse().getCode(),
                                       enrollment.getCourse().getCode()));
        }
    }

    private void requireWithinMaxMarks(MarkRequest request, Assessment assessment) {
        if (request.getMarksObtained().compareTo(assessment.getMaxMarks()) > 0) {
            throw new BusinessRuleException(
                    "Marks obtained (%s) cannot exceed the maximum for '%s' (%s)"
                            .formatted(request.getMarksObtained().toPlainString(),
                                       assessment.getTitle(),
                                       assessment.getMaxMarks().toPlainString()));
        }
    }

    /** Null for an admin with no teacher profile - the mark is simply unattributed. */
    private Teacher resolveEnteredBy(UserPrincipal caller) {
        if (ownership.isTeacher(caller)) {
            try {
                return ownership.requireTeacher(caller);
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }

    private Mark getOrThrow(Long id) {
        return markRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mark", "id", id));
    }

    private Enrollment findEnrollment(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
    }

    private Assessment findAssessment(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", id));
    }
}
