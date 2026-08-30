package com.activitytracking.activity.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ActivityEntryRequest {

    @NotNull(message = "Activity date is required")
    private LocalDate activityDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Activity type is required")
    private Long activityTypeId;

    @NotNull(message = "Activity subject is required")
    private Long activitySubjectId;

    @NotBlank(message = "Task description is required")
    private String taskDescription;
}