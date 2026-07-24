package com.group6.sams.service.impl;

import com.group6.sams.dto.request.AssessmentRequest;
import com.group6.sams.dto.response.AssessmentResponse;
import com.group6.sams.entity.Assessment;
import com.group6.sams.entity.Course;
import com.group6.sams.entity.Mark;
import com.group6.sams.entity.enums.AssessmentType;
import com.group6.sams.exception.BusinessRuleException;
import com.group6.sams.exception.DuplicateResourceException;
import com.group6.sams.exception.ResourceNotFoundException;
import com.group6.sams.mapper.GradeMapper;
import com.group6.sams.repository.AssessmentRepository;
import com.group6.sams.repository.CourseRepository;
import com.group6.sams.repository.MarkRepository;
import com.group6.sams.security.OwnershipService;
import com.group6.sams.security.UserPrincipal;
import com.group6.sams.service.AssessmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Assessment definitions. Owner: Member 3.
 *
 * Holds the rule the schema cannot express: the weights of a course's assessments
 * must not exceed 100 in total. A CHECK constraint sees only its own row, so it
 * can never know what its siblings already claim.
 */
@Service
public class AssessmentServiceImpl implements AssessmentService {

    private static final Logger log = LoggerFactory.getLogger(AssessmentServiceImpl.class);
    private static final BigDecimal MAX_TOTAL_WEIGHT = new BigDecimal("100");

    private final AssessmentRepository assessmentRepository;
    private final CourseRepository courseRepository;
    private final MarkRepository markRepository;
    private final OwnershipService ownership;

    public AssessmentServiceImpl(AssessmentRepository assessmentRepository,
                                 CourseRepository courseRepository,
                                 MarkRepository markRepository,
                                 OwnershipService ownership) {
        this.assessmentRepository = assessmentRepository;
        this.courseRepository = courseRepository;
        this.markRepository = markRepository;
        this.ownership = ownership;
    }

    /**
     * Readable by anyone with a legitimate interest in the course, including the
     * enrolled students - knowing that a final is worth 50% is not privileged
     * information, and students need it to understand their own grade.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AssessmentResponse> findByCourse(Long courseId, UserPrincipal caller) {
        findCourse(courseId);
        return assessmentRepository.findByCourseId(courseId).stream()
                .map(GradeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentResponse findById(Long id, UserPrincipal caller) {
        return GradeMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional
    public AssessmentResponse create(AssessmentRequest request, UserPrincipal caller) {
        Course course = findCourse(request.getCourseId());
        ownership.requireCourseAccess(caller, course);

        if (assessmentRepository.existsByCourseIdAndTitle(course.getId(), request.getTitle())) {
            throw new DuplicateResourceException(
                    "Course '%s' already has an assessment titled '%s'"
                            .formatted(course.getCode(), request.getTitle()));
        }

        requireWeightFits(course, request.getWeightPercent(), null);

        Assessment assessment = Assessment.builder()
                .course(course)
                .title(request.getTitle())
                .type(AssessmentType.valueOf(request.getType()))
                .maxMarks(request.getMaxMarks())
                .weightPercent(request.getWeightPercent())
                .assessedOn(request.getAssessedOn())
                .build();

        return GradeMapper.toResponse(assessmentRepository.save(assessment));
    }

    @Override
    @Transactional
    public AssessmentResponse update(Long id, AssessmentRequest request, UserPrincipal caller) {
        Assessment assessment = getOrThrow(id);
        Course course = assessment.getCourse();
        ownership.requireCourseAccess(caller, course);

        if (!assessment.getTitle().equals(request.getTitle())
                && assessmentRepository.existsByCourseIdAndTitle(
                        course.getId(), request.getTitle())) {
            throw new DuplicateResourceException(
                    "Course '%s' already has an assessment titled '%s'"
                            .formatted(course.getCode(), request.getTitle()));
        }

        // Excludes this row from the running total, otherwise saving without
        // changing the weight would count it twice.
        requireWeightFits(course, request.getWeightPercent(), assessment.getId());

        // Lowering maxMarks below a score already awarded would leave a student
        // holding more than the maximum, which is exactly the invalid state the
        // create/update path is meant to prevent.
        requireMaxMarksNotBelowRecordedScores(assessment, request.getMaxMarks());

        assessment.setTitle(request.getTitle());
        assessment.setType(AssessmentType.valueOf(request.getType()));
        assessment.setMaxMarks(request.getMaxMarks());
        assessment.setWeightPercent(request.getWeightPercent());
        assessment.setAssessedOn(request.getAssessedOn());

        return GradeMapper.toResponse(assessmentRepository.save(assessment));
    }

    /**
     * Deleting an assessment removes the marks awarded for it, so it is refused
     * while any exist. Silently discarding student scores is never the right
     * default; the teacher must remove the marks deliberately first.
     */
    @Override
    @Transactional
    public void delete(Long id, UserPrincipal caller) {
        Assessment assessment = getOrThrow(id);
        ownership.requireCourseAccess(caller, assessment.getCourse());

        List<Mark> marks = markRepository.findByAssessmentId(id);
        if (!marks.isEmpty()) {
            throw new BusinessRuleException(
                    "Cannot delete assessment '%s': %d mark(s) have been entered for it. Remove those marks first."
                            .formatted(assessment.getTitle(), marks.size()));
        }

        assessmentRepository.delete(assessment);
        log.info("Deleted assessment {} from course {}",
                 assessment.getTitle(), assessment.getCourse().getCode());
    }

    private void requireWeightFits(Course course, BigDecimal newWeight, Long excludeId) {
        BigDecimal existing = assessmentRepository.sumWeightByCourse(course.getId(), excludeId);
        BigDecimal total = existing.add(newWeight);

        if (total.compareTo(MAX_TOTAL_WEIGHT) > 0) {
            throw new BusinessRuleException(
                    "Total assessment weight for course '%s' would become %s%%, which exceeds 100%%. Currently allocated: %s%%."
                            .formatted(course.getCode(), total.stripTrailingZeros().toPlainString(),
                                       existing.stripTrailingZeros().toPlainString()));
        }
    }

    private void requireMaxMarksNotBelowRecordedScores(Assessment assessment,
                                                       BigDecimal newMaxMarks) {
        markRepository.findByAssessmentId(assessment.getId()).stream()
                .map(Mark::getMarksObtained)
                .max(BigDecimal::compareTo)
                .ifPresent(highest -> {
                    if (newMaxMarks.compareTo(highest) < 0) {
                        throw new BusinessRuleException(
                                "Cannot reduce maximum marks to %s: a score of %s has already been awarded."
                                        .formatted(newMaxMarks.toPlainString(),
                                                   highest.toPlainString()));
                    }
                });
    }

    private Assessment getOrThrow(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", id));
    }

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
    }
}
