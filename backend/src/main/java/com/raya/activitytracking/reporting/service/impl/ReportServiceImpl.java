package com.raya.activitytracking.reporting.service.impl;

import com.raya.activitytracking.activity.entity.ActivityEntry;
import com.raya.activitytracking.activity.repository.ActivityEntryRepository;
import com.raya.activitytracking.reporting.dto.response.ActivityDetailResponse;
import com.raya.activitytracking.reporting.dto.response.DailyTrendResponse;
import com.raya.activitytracking.reporting.dto.response.HoursBySubjectResponse;
import com.raya.activitytracking.reporting.dto.response.HoursByTypeResponse;
import com.raya.activitytracking.reporting.dto.response.MonthlyDetailsResponse;
import com.raya.activitytracking.reporting.dto.response.MonthlySummaryResponse;
import com.raya.activitytracking.reporting.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ActivityEntryRepository activityEntryRepository;

    // Temporary user ID for pre-authentication phase
    private static final Long TEMP_USER_ID = 1L;

    @Override
    public MonthlySummaryResponse getMonthlySummary(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<ActivityEntry> entries = activityEntryRepository
                .findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(TEMP_USER_ID, start, end);

        // Calculate total days (distinct dates)
        long totalDays = entries.stream()
                .map(ActivityEntry::getDate)
                .distinct()
                .count();

        // Calculate total duration (sum of all durations)
        long totalDurationMinutes = entries.stream()
                .mapToLong(ActivityEntry::getDurationMinutes)
                .sum();

        // Calculate average duration per day
        long averageDurationMinutes = totalDays > 0
                ? totalDurationMinutes / totalDays
                : 0;

        return MonthlySummaryResponse.builder()
                .month(month.toString())
                .totalDays((int) totalDays)
                .totalDurationMinutes(totalDurationMinutes)
                .averageDurationMinutes(averageDurationMinutes)
                .build();
    }

    @Override
    public List<HoursByTypeResponse> getHoursByType(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<Object[]> results = activityEntryRepository.sumDurationByTypeBetween(TEMP_USER_ID, start, end);

        return results.stream()
                .map(row -> HoursByTypeResponse.builder()
                        .activityType((String) row[0])
                        .totalDurationMinutes((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<HoursBySubjectResponse> getHoursBySubject(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<Object[]> results = activityEntryRepository.sumDurationBySubjectBetween(TEMP_USER_ID, start, end);

        return results.stream()
                .map(row -> HoursBySubjectResponse.builder()
                        .activitySubject((String) row[0])
                        .totalDurationMinutes((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DailyTrendResponse> getDailyTrend(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<ActivityEntry> entries = activityEntryRepository
                .findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(TEMP_USER_ID, start, end);

        // Group by date and sum durations
        Map<LocalDate, Long> dailyTotals = entries.stream()
                .collect(Collectors.groupingBy(
                        ActivityEntry::getDate,
                        Collectors.summingLong(ActivityEntry::getDurationMinutes)
                ));

        // Convert map to list of responses, sorted by date
        return dailyTotals.entrySet().stream()
                .map(entry -> DailyTrendResponse.builder()
                        .date(entry.getKey())
                        .totalDurationMinutes(entry.getValue())
                        .build())
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
    }

    @Override
    public MonthlyDetailsResponse getMonthlyDetails(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<ActivityEntry> entries = activityEntryRepository
                .findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(TEMP_USER_ID, start, end);

        List<ActivityDetailResponse> activities = entries.stream()
                .map(entry -> ActivityDetailResponse.builder()
                        .id(entry.getId())
                        .date(entry.getDate())
                        .startTime(entry.getStartTime())
                        .endTime(entry.getEndTime())
                        .activityType(entry.getActivityType().getName())
                        .activitySubject(entry.getActivitySubject().getName())
                        .taskDescription(entry.getTaskDescription())
                        .durationMinutes(entry.getDurationMinutes())
                        .build())
                .collect(Collectors.toList());

        return MonthlyDetailsResponse.builder()
                .month(month.toString())
                .activities(activities)
                .build();
    }
}
