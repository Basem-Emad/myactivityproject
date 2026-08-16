package com.raya.activitytracking.reporting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySummaryResponse {

    private String month;
    private int totalDays;
    private long totalDurationMinutes;
    private long averageDurationMinutes;
}
