package com.group6.sams.util;

import com.group6.sams.entity.Attendance;
import com.group6.sams.entity.enums.AttendanceStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

/**
 * Attendance percentage rules. Owner: Member 3.
 *
 * Single source of truth for two decisions the viva will ask about:
 *
 *  1. LATE counts as present. A student who arrived late did attend; penalising
 *     them identically to an absentee would misrepresent the record. This matches
 *     app.attendance.late-counts-as-present in application.yml.
 *
 *  2. An enrollment with no attendance records has NO percentage, which is not the
 *     same as 0%. Zero would mean "attended nothing"; null means "nothing was ever
 *     recorded". Reporting a brand-new enrollment as 0% would flood the
 *     low-attendance alert list with students who have not had a class yet.
 */
public final class AttendanceUtil {

    private AttendanceUtil() {
    }

    public static boolean countsAsPresent(AttendanceStatus status) {
        return status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE;
    }

    /**
     * @return percentage rounded to 2 decimal places, or null when there are no
     *         records at all. Callers must handle null explicitly.
     */
    public static BigDecimal percentage(long attended, long total) {
        if (total <= 0) return null;
        return BigDecimal.valueOf(attended)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal percentage(Collection<Attendance> records) {
        if (records == null || records.isEmpty()) return null;
        long attended = records.stream()
                .filter(a -> countsAsPresent(a.getStatus()))
                .count();
        return percentage(attended, records.size());
    }

    /** False when percentage is null - "no data" is not "below threshold". */
    public static boolean isBelowThreshold(BigDecimal percentage, BigDecimal threshold) {
        return percentage != null && percentage.compareTo(threshold) < 0;
    }
}
