package com.raya.activitytracking.activity.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityEntryResponse {

    private Long id;
    private Long userId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes; // exact duration in minutes
    private String durationFormatted; // "3h 30m" for the frontend
    private Long activityTypeId;
    private String activityTypeName;
    private Long activitySubjectId;
    private String activitySubjectName;
    private String taskDescription;
}
