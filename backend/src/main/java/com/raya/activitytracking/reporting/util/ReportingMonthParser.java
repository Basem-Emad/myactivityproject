package com.raya.activitytracking.reporting.util;

import java.time.YearMonth;
import java.util.regex.Pattern;

public final class ReportingMonthParser {

    private static final Pattern MONTH_PATTERN = Pattern.compile("^\\d{4}-\\d{2}$");
    private static final String ERROR_MESSAGE = "Invalid month format. Expected YYYY-MM (e.g. 2026-08).";

    private ReportingMonthParser() {
    }

    public static YearMonth parse(String month) {
        if (month == null || !MONTH_PATTERN.matcher(month).matches()) {
            throw new IllegalArgumentException(ERROR_MESSAGE);
        }
        int year = Integer.parseInt(month.substring(0, 4));
        int monthValue = Integer.parseInt(month.substring(5, 7));
        if (monthValue < 1 || monthValue > 12) {
            throw new IllegalArgumentException(ERROR_MESSAGE);
        }
        return YearMonth.of(year, monthValue);
    }
}
