package com.raya.activitytracking.activity.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEntryRequest {

    @NotNull(message = "Date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(type = "string", example = "2026-08-20")
    private LocalDate date;

    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "H:mm[:ss]")
    @Schema(type = "string", example = "09:00:00")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "H:mm[:ss]")
    @Schema(type = "string", example = "17:00:00")
    private LocalTime endTime;

    @NotNull(message = "Activity type is required")
    private Long activityTypeId;

    @NotNull(message = "Activity subject is required")
    private Long activitySubjectId;

    @NotBlank(message = "Task description is required")
    @Size(max = 2000, message = "Task description must not exceed 2000 characters")
    private String taskDescription;
}
