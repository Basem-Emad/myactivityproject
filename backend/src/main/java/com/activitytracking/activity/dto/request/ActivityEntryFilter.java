package com.activitytracking.activity.dto.request;

import java.time.LocalDate;

public record ActivityEntryFilter(
        Long userId,
        LocalDate date,
        LocalDate fromDate,
        LocalDate toDate,
        Long activityTypeId,
        Long activitySubjectId
) {
}
