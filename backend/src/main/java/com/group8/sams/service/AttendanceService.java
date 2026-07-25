package com.group8.sams.service;

import com.group8.sams.dto.request.AttendanceRequest;
import com.group8.sams.dto.request.BulkAttendanceRequest;
import com.group8.sams.dto.response.AttendanceResponse;
import com.group8.sams.dto.response.AttendanceSummaryResponse;
import com.group8.sams.security.UserPrincipal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Every method takes the caller, because attendance authorization is not just
 * about roles - it depends on which course the record belongs to and which student
 * it is about.
 */
public interface AttendanceService {

    AttendanceResponse record(AttendanceRequest request, UserPrincipal caller);

    List<AttendanceResponse> recordBulk(BulkAttendanceRequest request, UserPrincipal caller);

    AttendanceResponse update(Long id, AttendanceRequest request, UserPrincipal caller);

    void delete(Long id, UserPrincipal caller);

    List<AttendanceResponse> findByCourseAndDate(Long courseId, LocalDate date,
                                                 UserPrincipal caller);

    List<AttendanceResponse> findByStudent(Long studentId, UserPrincipal caller);

    AttendanceSummaryResponse percentage(Long studentId, Long courseId, UserPrincipal caller);

    List<AttendanceSummaryResponse> courseSummary(Long courseId, UserPrincipal caller);

    List<AttendanceSummaryResponse> lowAttendance(BigDecimal threshold, UserPrincipal caller);
}
