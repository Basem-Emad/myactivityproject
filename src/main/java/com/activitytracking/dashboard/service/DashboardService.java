package com.activitytracking.dashboard.service;

import com.activitytracking.dashboard.dto.response.HoursBySubjectResponse;
import com.activitytracking.dashboard.dto.response.HoursByTypeResponse;
import com.activitytracking.dashboard.dto.response.MonthlySummaryResponse;

import java.time.YearMonth;
import java.util.List;

public interface DashboardService {
    MonthlySummaryResponse getMonthlySummary(Long userId, YearMonth month);
    List<HoursByTypeResponse> getHoursByType(Long userId, YearMonth month);
    List<HoursBySubjectResponse> getHoursBySubject(Long userId, YearMonth month);
}