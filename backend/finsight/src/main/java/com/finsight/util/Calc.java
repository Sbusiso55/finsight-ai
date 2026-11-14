package com.finsight.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/** Safe percentage maths and rand formatting. Percentages are returned as e.g. 36.29 (not 0.3629). */
public final class Calc {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private Calc() { }

    /** part / whole x 100, or null when whole is zero or either value is missing. */
    public static BigDecimal percentOf(BigDecimal part, BigDecimal whole) {
        if (part == null || whole == null || whole.signum() == 0) return null;
        return part.multiply(HUNDRED).divide(whole, 2, RoundingMode.HALF_UP);
    }

    /** (current - previous) / previous x 100, or null when previous is zero or missing. */
    public static BigDecimal growthPercent(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null || previous.signum() == 0) return null;
        return current.subtract(previous).multiply(HUNDRED).divide(previous, 2, RoundingMode.HALF_UP);
    }

    /** "R9,000" for whole amounts, "R9,000.50" otherwise. */
    public static String money(BigDecimal value) {
        BigDecimal v = value.setScale(2, RoundingMode.HALF_UP);
        boolean whole = v.stripTrailingZeros().scale() <= 0;
        return String.format(Locale.US, whole ? "R%,.0f" : "R%,.2f", v);
    }

    /** "36.29%". */
    public static String percent(BigDecimal value) {
        return String.format(Locale.US, "%.2f%%", value);
    }
}
