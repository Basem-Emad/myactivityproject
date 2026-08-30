package com.activitytracking.dashboard.service.impl;

import com.activitytracking.activity.entity.ActivityEntry;
import com.activitytracking.activity.repository.ActivityEntryRepository;
import com.activitytracking.dashboard.dto.response.HoursBySubjectResponse;
import com.activitytracking.dashboard.dto.response.HoursByTypeResponse;
import com.activitytracking.dashboard.dto.response.MonthlySummaryResponse;
import com.activitytracking.dashboard.service.DashboardService;
import com.activitytracking.masterdata.entity.ActivitySubject;
import com.activitytracking.masterdata.entity.ActivityType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final ActivityEntryRepository activityEntryRepository;

    public DashboardServiceImpl(ActivityEntryRepository activityEntryRepository) {
        this.activityEntryRepository = activityEntryRepository;
    }

    @Override
    public MonthlySummaryResponse getMonthlySummary(Long userId, YearMonth month) {

        List<ActivityEntry> entries = findEntriesForMonth(userId, month);

        Set<LocalDate> uniqueDates = entries.stream()
                .map(ActivityEntry::getActivityDate)
                .collect(Collectors.toSet());

        long totalMinutes = entries.stream()
                .mapToLong(ActivityEntry::getDurationMinutes)
                .sum();

        return new MonthlySummaryResponse(uniqueDates.size(), totalMinutes);
    }

    @Override
    public List<HoursByTypeResponse> getHoursByType(Long userId, YearMonth month) {

        List<ActivityEntry> entries = findEntriesForMonth(userId, month);

        Map<String, Long> grouped = entries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getActivityType().getName(),
                        Collectors.summingLong(ActivityEntry::getDurationMinutes)
                ));

        return grouped.entrySet().stream()
                .map(e -> new HoursByTypeResponse(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public List<HoursBySubjectResponse> getHoursBySubject(Long userId, YearMonth month) {

        List<ActivityEntry> entries = findEntriesForMonth(userId, month);

        Map<String, Long> grouped = entries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getActivitySubject().getName(),
                        Collectors.summingLong(ActivityEntry::getDurationMinutes)
                ));

        return grouped.entrySet().stream()
                .map(e -> new HoursBySubjectResponse(e.getKey(), e.getValue()))
                .toList();
    }

    private List<ActivityEntry> findEntriesForMonth(Long userId, YearMonth month) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();
        return activityEntryRepository.findByUserIdAndActivityDateBetween(userId, startDate, endDate);
    }
}