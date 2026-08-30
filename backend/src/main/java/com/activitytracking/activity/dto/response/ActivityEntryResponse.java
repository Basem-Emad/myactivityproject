package com.activitytracking.activity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class ActivityEntryResponse {

    private Long id;
    private LocalDate activityDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;
    private String taskDescription;

    private Long userId;
    private String userName;

    private Long activityTypeId;
    private String activityTypeName;

    private Long activitySubjectId;
    private String activitySubjectName;
}