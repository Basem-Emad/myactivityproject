package com.raya.activitytracking.reporting.service;

import com.raya.activitytracking.reporting.dto.response.DailyTrendResponse;
import com.raya.activitytracking.reporting.dto.response.HoursBySubjectResponse;
import com.raya.activitytracking.reporting.dto.response.HoursByTypeResponse;
import com.raya.activitytracking.reporting.dto.response.MonthlyDetailsResponse;
import com.raya.activitytracking.reporting.dto.response.MonthlySummaryResponse;

import java.time.YearMonth;
import java.util.List;

public interface ReportService {

    MonthlySummaryResponse getMonthlySummary(YearMonth month);

    List<HoursByTypeResponse> getHoursByType(YearMonth month);

    List<HoursBySubjectResponse> getHoursBySubject(YearMonth month);

    List<DailyTrendResponse> getDailyTrend(YearMonth month);

    MonthlyDetailsResponse getMonthlyDetails(YearMonth month);
}
