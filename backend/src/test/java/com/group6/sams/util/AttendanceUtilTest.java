package com.group6.sams.util;

import com.group6.sams.entity.enums.AttendanceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Attendance percentage rules. No database, no Spring context.
 *
 * These figures are hand-calculable, which is exactly what makes them defensible
 * in the viva: "18 of 20 sessions attended is 90%" can be checked on paper.
 */
class AttendanceUtilTest {

    @Test
    @DisplayName("LATE counts as present, ABSENT does not")
    void lateCountsAsPresent() {
        assertTrue(AttendanceUtil.countsAsPresent(AttendanceStatus.PRESENT));
        assertTrue(AttendanceUtil.countsAsPresent(AttendanceStatus.LATE));
        assertFalse(AttendanceUtil.countsAsPresent(AttendanceStatus.ABSENT));
    }

    @Test
    @DisplayName("18 attended of 20 sessions is 90.00%")
    void computesPercentage() {
        assertEquals(0, AttendanceUtil.percentage(18, 20).compareTo(new BigDecimal("90.00")));
    }

    @Test
    @DisplayName("Rounds to two decimal places, half up")
    void roundsToTwoDecimals() {
        // 2/3 = 66.666... -> 66.67
        assertEquals(0, AttendanceUtil.percentage(2, 3).compareTo(new BigDecimal("66.67")));
    }

    @Test
    @DisplayName("Full and zero attendance produce 100 and 0")
    void handlesExtremes() {
        assertEquals(0, AttendanceUtil.percentage(10, 10).compareTo(new BigDecimal("100.00")));
        assertEquals(0, AttendanceUtil.percentage(0, 10).compareTo(new BigDecimal("0.00")));
    }

    @Test
    @DisplayName("No recorded sessions yields null, not zero")
    void noRecordsIsNullNotZero() {
        assertNull(AttendanceUtil.percentage(0, 0),
                "An enrollment with no attendance has no percentage; 0% would wrongly "
                + "mean the student attended nothing");
    }

    @Test
    @DisplayName("A null percentage is never below the threshold")
    void nullIsNotBelowThreshold() {
        assertFalse(AttendanceUtil.isBelowThreshold(null, new BigDecimal("75")),
                "Students with no records must not appear in low-attendance alerts");
    }

    @Test
    @DisplayName("Threshold comparison is strict: exactly 75 is not below 75")
    void thresholdBoundary() {
        BigDecimal threshold = new BigDecimal("75");
        assertFalse(AttendanceUtil.isBelowThreshold(new BigDecimal("75.00"), threshold));
        assertTrue(AttendanceUtil.isBelowThreshold(new BigDecimal("74.99"), threshold));
        assertFalse(AttendanceUtil.isBelowThreshold(new BigDecimal("75.01"), threshold));
    }
}
