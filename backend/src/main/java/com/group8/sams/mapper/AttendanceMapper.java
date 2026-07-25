package com.group8.sams.mapper;

import com.group8.sams.dto.response.AttendanceResponse;
import com.group8.sams.entity.*;

/** Owner: Member 3. Must be called inside a transaction - associations are LAZY. */
public final class AttendanceMapper {

    private AttendanceMapper() {
    }

    public static AttendanceResponse toResponse(Attendance a) {
        Enrollment e = a.getEnrollment();
        Student s = e.getStudent();
        Course c = e.getCourse();
        Teacher recorder = a.getRecordedBy();

        return AttendanceResponse.builder()
                .id(a.getId())
                .attendanceDate(a.getAttendanceDate())
                .status(a.getStatus().name())
                .remarks(a.getRemarks())
                .enrollmentId(e.getId())
                .studentId(s.getId())
                .studentName(s.getFullName())
                .rollNumber(s.getRollNumber())
                .courseId(c.getId())
                .courseCode(c.getCode())
                .courseTitle(c.getTitle())
                .recordedBy(recorder != null ? recorder.getFullName() : null)
                .build();
    }
}
