package com.group6.sams.service.impl;

import com.group6.sams.dto.request.AttendanceRequest;
import com.group6.sams.dto.request.BulkAttendanceRequest;
import com.group6.sams.dto.response.AttendanceResponse;
import com.group6.sams.dto.response.AttendanceSummaryResponse;
import com.group6.sams.entity.*;
import com.group6.sams.entity.enums.AttendanceStatus;
import com.group6.sams.exception.BusinessRuleException;
import com.group6.sams.exception.DuplicateResourceException;
import com.group6.sams.exception.ResourceNotFoundException;
import com.group6.sams.mapper.AttendanceMapper;
import com.group6.sams.repository.*;
import com.group6.sams.security.OwnershipService;
import com.group6.sams.security.UserPrincipal;
import com.group6.sams.service.AttendanceService;
import com.group6.sams.util.AttendanceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Attendance business logic. Owner: Member 3.
 *
 * Authorization here is two-layered. SecurityConfig already refused anyone without
 * TEACHER or ADMIN on the write endpoints; this class adds the part a role check
 * cannot express - that the teacher is assigned to the specific course, and that a
 * student is reading only their own record.
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceServiceImpl.class);

    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final OwnershipService ownership;
    private final BigDecimal defaultThreshold;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 CourseRepository courseRepository,
                                 OwnershipService ownership,
                                 @Value("${app.attendance.threshold}") BigDecimal defaultThreshold) {
        this.attendanceRepository = attendanceRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.ownership = ownership;
        this.defaultThreshold = defaultThreshold;
    }

    @Override
    @Transactional
    public AttendanceResponse record(AttendanceRequest request, UserPrincipal caller) {
        Enrollment enrollment = findEnrollment(request.getEnrollmentId());
        ownership.requireCourseAccess(caller, enrollment.getCourse());

        if (attendanceRepository.existsByEnrollmentIdAndAttendanceDate(
                enrollment.getId(), request.getAttendanceDate())) {
            throw new DuplicateResourceException(
                    "Attendance for %s on %s has already been recorded. Use PUT to change it."
                            .formatted(enrollment.getStudent().getRollNumber(),
                                       request.getAttendanceDate()));
        }

        Attendance attendance = Attendance.builder()
                .enrollment(enrollment)
                .attendanceDate(request.getAttendanceDate())
                .status(AttendanceStatus.valueOf(request.getStatus()))
                .remarks(request.getRemarks())
                .recordedBy(resolveRecorder(caller))
                .build();

        return AttendanceMapper.toResponse(attendanceRepository.save(attendance));
    }

    /**
     * One transaction for the whole roster. If any entry is rejected - a duplicate,
     * or an enrollment that belongs to a different course - nothing is saved, so the
     * register never ends up half filled.
     */
    @Override
    @Transactional
    public List<AttendanceResponse> recordBulk(BulkAttendanceRequest request,
                                               UserPrincipal caller) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course", "id", request.getCourseId()));
        ownership.requireCourseAccess(caller, course);

        Teacher recorder = resolveRecorder(caller);
        List<Attendance> batch = new ArrayList<>();

        for (BulkAttendanceRequest.Entry entry : request.getEntries()) {
            Enrollment enrollment = findEnrollment(entry.getEnrollmentId());

            // Guards against a payload that mixes another course's roster into this
            // course's register.
            if (!enrollment.getCourse().getId().equals(course.getId())) {
                throw new BusinessRuleException(
                        "Enrollment %d does not belong to course '%s'"
                                .formatted(entry.getEnrollmentId(), course.getCode()));
            }

            if (attendanceRepository.existsByEnrollmentIdAndAttendanceDate(
                    enrollment.getId(), request.getAttendanceDate())) {
                throw new DuplicateResourceException(
                        "Attendance for %s on %s already exists"
                                .formatted(enrollment.getStudent().getRollNumber(),
                                           request.getAttendanceDate()));
            }

            batch.add(Attendance.builder()
                    .enrollment(enrollment)
                    .attendanceDate(request.getAttendanceDate())
                    .status(AttendanceStatus.valueOf(entry.getStatus()))
                    .remarks(entry.getRemarks())
                    .recordedBy(recorder)
                    .build());
        }

        List<Attendance> saved = attendanceRepository.saveAll(batch);
        log.info("Recorded {} attendance entries for course {} on {}",
                 saved.size(), course.getCode(), request.getAttendanceDate());

        return saved.stream().map(AttendanceMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public AttendanceResponse update(Long id, AttendanceRequest request, UserPrincipal caller) {
        Attendance attendance = findAttendance(id);
        ownership.requireCourseAccess(caller, attendance.getEnrollment().getCourse());

        // The enrollment and date identify the record; changing them would collide
        // with the unique constraint or silently move the record to another student.
        attendance.setStatus(AttendanceStatus.valueOf(request.getStatus()));
        attendance.setRemarks(request.getRemarks());
        attendance.setRecordedBy(resolveRecorder(caller));

        return AttendanceMapper.toResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional
    public void delete(Long id, UserPrincipal caller) {
        Attendance attendance = findAttendance(id);
        ownership.requireCourseAccess(caller, attendance.getEnrollment().getCourse());
        attendanceRepository.delete(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> findByCourseAndDate(Long courseId, LocalDate date,
                                                        UserPrincipal caller) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        ownership.requireCourseAccess(caller, course);

        List<Attendance> records = (date != null)
                ? attendanceRepository.findByEnrollmentCourseIdAndAttendanceDate(courseId, date)
                : attendanceRepository.findByEnrollmentCourseId(courseId);

        return records.stream().map(AttendanceMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> findByStudent(Long studentId, UserPrincipal caller) {
        ownership.requireStudentAccess(caller, studentId);
        return attendanceRepository.findByEnrollmentStudentId(studentId).stream()
                .map(AttendanceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummaryResponse percentage(Long studentId, Long courseId,
                                                UserPrincipal caller) {
        ownership.requireStudentAccess(caller, studentId);

        Enrollment enrollment = enrollmentRepository.findByStudentId(studentId).stream()
                .filter(e -> e.getCourse().getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "This student is not enrolled in course id " + courseId));

        return summarize(enrollment, defaultThreshold);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceSummaryResponse> courseSummary(Long courseId, UserPrincipal caller) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        ownership.requireCourseAccess(caller, course);

        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(e -> summarize(e, defaultThreshold))
                .toList();
    }

    /**
     * Students whose attendance has fallen below the threshold.
     *
     * A teacher sees only their own courses; an admin sees everything. Enrollments
     * with no attendance records are excluded rather than reported as 0% - see
     * AttendanceUtil for why that distinction matters.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceSummaryResponse> lowAttendance(BigDecimal threshold,
                                                         UserPrincipal caller) {
        BigDecimal limit = (threshold != null) ? threshold : defaultThreshold;

        List<Enrollment> scope;
        if (ownership.isAdmin(caller)) {
            scope = enrollmentRepository.findAll();
        } else {
            Teacher teacher = ownership.requireTeacher(caller);
            scope = courseRepository.findByTeacherId(teacher.getId()).stream()
                    .flatMap(c -> enrollmentRepository.findByCourseId(c.getId()).stream())
                    .toList();
        }

        return scope.stream()
                .map(e -> summarize(e, limit))
                .filter(AttendanceSummaryResponse::isBelowThreshold)
                .toList();
    }

    private AttendanceSummaryResponse summarize(Enrollment enrollment, BigDecimal threshold) {
        List<Attendance> records = attendanceRepository.findByEnrollmentId(enrollment.getId());

        long present = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long absent = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        long late = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        long attended = present + late;

        BigDecimal percentage = AttendanceUtil.percentage(attended, records.size());

        Student s = enrollment.getStudent();
        Course c = enrollment.getCourse();

        return AttendanceSummaryResponse.builder()
                .studentId(s.getId())
                .studentName(s.getFullName())
                .rollNumber(s.getRollNumber())
                .courseId(c.getId())
                .courseCode(c.getCode())
                .courseTitle(c.getTitle())
                .enrollmentId(enrollment.getId())
                .totalSessions(records.size())
                .presentCount(present)
                .absentCount(absent)
                .lateCount(late)
                .attendedCount(attended)
                .percentage(percentage)
                .belowThreshold(AttendanceUtil.isBelowThreshold(percentage, threshold))
                .build();
    }

    /** Null for an admin who has no teacher profile - the record is simply unattributed. */
    private Teacher resolveRecorder(UserPrincipal caller) {
        if (ownership.isTeacher(caller)) {
            try {
                return ownership.requireTeacher(caller);
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }

    private Enrollment findEnrollment(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
    }

    private Attendance findAttendance(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));
    }
}
