package com.group6.sams.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Grading logic. No database, no Spring context.
 *
 * The boundary cases are the important ones: 49 vs 50 and 89 vs 90 are exactly
 * where a grading bug would be invisible in ordinary testing but very visible to a
 * student who was given the wrong grade.
 */
class GradeUtilTest {

    @Test
    @DisplayName("Letter grades at every boundary")
    void letterGradeBoundaries() {
        assertEquals("A+", GradeUtil.letterGrade(new BigDecimal("100")));
        assertEquals("A+", GradeUtil.letterGrade(new BigDecimal("90")));
        assertEquals("A",  GradeUtil.letterGrade(new BigDecimal("89.99")));
        assertEquals("A",  GradeUtil.letterGrade(new BigDecimal("80")));
        assertEquals("B",  GradeUtil.letterGrade(new BigDecimal("79.99")));
        assertEquals("B",  GradeUtil.letterGrade(new BigDecimal("70")));
        assertEquals("C",  GradeUtil.letterGrade(new BigDecimal("69.99")));
        assertEquals("C",  GradeUtil.letterGrade(new BigDecimal("60")));
        assertEquals("D",  GradeUtil.letterGrade(new BigDecimal("59.99")));
        assertEquals("D",  GradeUtil.letterGrade(new BigDecimal("50")));
        assertEquals("F",  GradeUtil.letterGrade(new BigDecimal("49.99")));
        assertEquals("F",  GradeUtil.letterGrade(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Grade points match the letter scale")
    void gradePointsScale() {
        assertEquals(0, GradeUtil.gradePoints(new BigDecimal("95")).compareTo(new BigDecimal("4.0")));
        assertEquals(0, GradeUtil.gradePoints(new BigDecimal("85")).compareTo(new BigDecimal("3.7")));
        assertEquals(0, GradeUtil.gradePoints(new BigDecimal("75")).compareTo(new BigDecimal("3.3")));
        assertEquals(0, GradeUtil.gradePoints(new BigDecimal("65")).compareTo(new BigDecimal("2.7")));
        assertEquals(0, GradeUtil.gradePoints(new BigDecimal("55")).compareTo(new BigDecimal("2.0")));
        assertEquals(0, GradeUtil.gradePoints(new BigDecimal("45")).compareTo(new BigDecimal("0.0")));
    }

    @Test
    @DisplayName("Pass boundary is exactly 50")
    void passBoundary() {
        assertTrue(GradeUtil.isPass(new BigDecimal("50")));
        assertFalse(GradeUtil.isPass(new BigDecimal("49.99")));
        assertNull(GradeUtil.isPass(null), "Not graded yet is neither pass nor fail");
    }

    @Test
    @DisplayName("Weighted percentage respects assessment weight, not raw marks")
    void weightedPercentage() {
        // Quiz  : 8/10  weight 20 -> 0.80 * 20 = 16
        // Final : 60/100 weight 80 -> 0.60 * 80 = 48
        // total weighted score 64 of weight 100 -> 64.00%
        BigDecimal score = new BigDecimal("64");
        BigDecimal weight = new BigDecimal("100");
        assertEquals(0, GradeUtil.weightedPercentage(score, weight)
                                 .compareTo(new BigDecimal("64.00")));
    }

    @Test
    @DisplayName("Partial grading divides by recorded weight, not by 100")
    void partialGrading() {
        // Only the 20% quiz is marked, scored 18/20 of its weight -> 90% so far.
        BigDecimal score = new BigDecimal("18");
        BigDecimal weight = new BigDecimal("20");
        assertEquals(0, GradeUtil.weightedPercentage(score, weight)
                                 .compareTo(new BigDecimal("90.00")),
                "A student with only one assessment marked should not be penalised "
                + "for assessments that have not happened yet");
    }

    @Test
    @DisplayName("Zero recorded weight yields null, not zero percent")
    void noWeightIsNull() {
        assertNull(GradeUtil.weightedPercentage(BigDecimal.ZERO, BigDecimal.ZERO));
        assertNull(GradeUtil.weightedPercentage(BigDecimal.TEN, null));
        assertNull(GradeUtil.letterGrade(null));
        assertNull(GradeUtil.gradePoints(null));
    }

    @Test
    @DisplayName("GPA is credit-weighted, so heavier courses move it more")
    void gpaIsCreditWeighted() {
        // A+ (4.0) on 3 credits and F (0.0) on 1 credit
        // = (4.0*3 + 0.0*1) / 4 = 12/4 = 3.00
        BigDecimal points = new BigDecimal("4.0").multiply(BigDecimal.valueOf(3))
                .add(new BigDecimal("0.0").multiply(BigDecimal.ONE));
        assertEquals(0, GradeUtil.gpa(points, 4).compareTo(new BigDecimal("3.00")));
    }

    @Test
    @DisplayName("Credit weighting differs from a plain average")
    void creditWeightingMatters() {
        // 4.0 on 4 credits, 2.0 on 1 credit
        // credit-weighted = (16 + 2)/5 = 3.60   plain average would be 3.00
        BigDecimal points = new BigDecimal("4.0").multiply(BigDecimal.valueOf(4))
                .add(new BigDecimal("2.0"));
        assertEquals(0, GradeUtil.gpa(points, 5).compareTo(new BigDecimal("3.60")));
    }

    @Test
    @DisplayName("No credits yields null GPA, not 0.00")
    void noCreditsIsNull() {
        assertNull(GradeUtil.gpa(BigDecimal.ZERO, 0),
                "A student with nothing graded has no GPA; 0.00 would read as total failure");
    }
}
