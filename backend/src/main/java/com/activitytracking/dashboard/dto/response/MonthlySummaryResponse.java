package com.activitytracking.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MonthlySummaryResponse {
    private int totalDays;
    private long totalHoursMinutes;
}