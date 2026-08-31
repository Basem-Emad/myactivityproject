package com.raya.activitytracking.reporting.util;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportingMonthParserTest {

    private static final String EXPECTED_MESSAGE = "Invalid month format. Expected YYYY-MM (e.g. 2026-08).";

    @Test
    void parse_shouldReturnYearMonth_whenAugust() {
        YearMonth result = ReportingMonthParser.parse("2026-08");
        assertThat(result).isEqualTo(YearMonth.of(2026, 8));
    }

    @Test
    void parse_shouldReturnYearMonth_whenJanuary() {
        YearMonth result = ReportingMonthParser.parse("2026-01");
        assertThat(result).isEqualTo(YearMonth.of(2026, 1));
    }

    @Test
    void parse_shouldReturnYearMonth_whenDecember() {
        YearMonth result = ReportingMonthParser.parse("2026-12");
        assertThat(result).isEqualTo(YearMonth.of(2026, 12));
    }

    @Test
    void parse_shouldReturnYearMonth_whenFutureMonth() {
        YearMonth result = ReportingMonthParser.parse("2099-06");
        assertThat(result).isEqualTo(YearMonth.of(2099, 6));
    }

    @Test
    void parse_shouldThrow_whenNull() {
        assertThatThrownBy(() -> ReportingMonthParser.parse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXPECTED_MESSAGE);
    }

    @Test
    void parse_shouldThrow_whenEmpty() {
        assertThatThrownBy(() -> ReportingMonthParser.parse(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXPECTED_MESSAGE);
    }

    @Test
    void parse_shouldThrow_whenBlank() {
        assertThatThrownBy(() -> ReportingMonthParser.parse(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXPECTED_MESSAGE);
    }

    @Test
    void parse_shouldThrow_whenSingleDigitMonth() {
        assertThatThrownBy(() -> ReportingMonthParser.parse("2026-8"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXPECTED_MESSAGE);
    }

    @Test
    void parse_shouldThrow_whenSlashFormat() {
        assertThatThrownBy(() -> ReportingMonthParser.parse("2026/08"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXPECTED_MESSAGE);
    }

    @Test
    void parse_shouldThrow_whenMonthIsZero() {
        assertThatThrownBy(() -> ReportingMonthParser.parse("2026-00"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXPECTED_MESSAGE);
    }

    @Test
    void parse_shouldThrow_whenMonthIsThirteen() {
        assertThatThrownBy(() -> ReportingMonthParser.parse("2026-13"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXPECTED_MESSAGE);
    }

    @Test
    void parse_shouldThrow_whenFullDate() {
        assertThatThrownBy(() -> ReportingMonthParser.parse("2026-08-01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXPECTED_MESSAGE);
    }

    @Test
    void parse_shouldThrow_whenRandomText() {
        assertThatThrownBy(() -> ReportingMonthParser.parse("abcd"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXPECTED_MESSAGE);
    }

    @Test
    void parse_shouldThrow_whenLeadingWhitespace() {
        assertThatThrownBy(() -> ReportingMonthParser.parse(" 2026-08"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXPECTED_MESSAGE);
    }

    @Test
    void parse_shouldThrow_whenTrailingWhitespace() {
        assertThatThrownBy(() -> ReportingMonthParser.parse("2026-08 "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EXPECTED_MESSAGE);
    }

    @Test
    void parse_shouldHaveExactExpectedErrorMessage() {
        assertThatThrownBy(() -> ReportingMonthParser.parse("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid month format. Expected YYYY-MM (e.g. 2026-08).");
    }
}
