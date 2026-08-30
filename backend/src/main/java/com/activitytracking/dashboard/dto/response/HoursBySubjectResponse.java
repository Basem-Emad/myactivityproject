package com.activitytracking.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HoursBySubjectResponse {
    private String activitySubjectName;
    private long hoursMinutes;
}