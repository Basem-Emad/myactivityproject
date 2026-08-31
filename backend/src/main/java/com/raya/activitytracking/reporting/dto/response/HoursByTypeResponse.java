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
public class HoursByTypeResponse {

    private String activityType;
    private long totalDurationMinutes;
}
