package com.activitytracking.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HoursByTypeResponse {
    private String activityTypeName;
    private long hoursMinutes;
}