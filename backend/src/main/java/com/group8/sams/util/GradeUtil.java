package com.group8.sams.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The single source of truth for grading. Owner: Member 3, consumed by Member 4.
 *
 * Member 4's transcript and GPA code calls this rather than re-implementing the
 * scale, and the boundaries are deliberately NOT duplicated into SQL CASE
 * expressions - two copies of a grade scale drift apart, and the reports would
 * then disagree with the marks page.
 *
 * Scale (approved in Phase 1):
 *   90-100  A+  4.0
 *   80-89   A   3.7
 *   70-79   B   3.3
 *   60-69   C   2.7
 *   50-59   D   2.0
 *   < 50    F   0.0
 */
public final class GradeUtil {

    public static final BigDecimal PASS_PERCENTAGE = new BigDecimal("50");

    private GradeUtil() {
    }

    /**
     * Weighted percentage for one course.
     *
     * Each assessment contributes (obtained / max) * weight. Using raw mark totals
     * instead would let a 10-mark quiz count the same as a 100-mark final.
     *
     * The divisor is the weight actually recorded so far, not a hard-coded 100, so
     * a mid-semester view is meaningful rather than artificially low.
     *
     * @return null when no weight has been recorded - "not graded yet" is not "zero".
     */
    public static BigDecimal weightedPercentage(BigDecimal weightedScore,
                                                BigDecimal totalWeight) {
        if (totalWeight == null || totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return weightedScore
                .multiply(BigDecimal.valueOf(100))
                .divide(totalWeight, 2, RoundingMode.HALF_UP);
    }

    /** Letter grade for a percentage. Null percentage yields null, never "F". */
    public static String letterGrade(BigDecimal percentage) {
        if (percentage == null) return null;
        double p = percentage.doubleValue();
        if (p >= 90) return "A+";
        if (p >= 80) return "A";
        if (p >= 70) return "B";
        if (p >= 60) return "C";
        if (p >= 50) return "D";
        return "F";
    }

    /** Grade points for a percentage. Null percentage yields null, never 0.0. */
    public static BigDecimal gradePoints(BigDecimal percentage) {
        if (percentage == null) return null;
        double p = percentage.doubleValue();
        if (p >= 90) return new BigDecimal("4.0");
        if (p >= 80) return new BigDecimal("3.7");
        if (p >= 70) return new BigDecimal("3.3");
        if (p >= 60) return new BigDecimal("2.7");
        if (p >= 50) return new BigDecimal("2.0");
        return new BigDecimal("0.0");
    }

    public static Boolean isPass(BigDecimal percentage) {
        if (percentage == null) return null;
        return percentage.compareTo(PASS_PERCENTAGE) >= 0;
    }

    /**
     * Credit-weighted GPA: sum(gradePoints * credits) / sum(credits).
     *
     * Credit weighting is the point: a 4-credit course must move the GPA more than
     * a 1-credit one. A plain average of grade points would be wrong.
     *
     * @return null when no credits contributed, so an ungraded student shows "no GPA"
     *         rather than 0.00, which would read as total failure.
     */
    public static BigDecimal gpa(BigDecimal totalWeightedPoints, int totalCredits) {
        if (totalCredits <= 0) return null;
        return totalWeightedPoints.divide(BigDecimal.valueOf(totalCredits), 2,
                                          RoundingMode.HALF_UP);
    }
}
