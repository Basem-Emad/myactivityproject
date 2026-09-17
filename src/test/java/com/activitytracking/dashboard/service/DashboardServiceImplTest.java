package com.activitytracking.dashboard.service;

import com.activitytracking.activity.entity.ActivityEntry;
import com.activitytracking.activity.repository.ActivityEntryRepository;
import com.activitytracking.dashboard.dto.response.HoursBySubjectResponse;
import com.activitytracking.dashboard.dto.response.HoursByTypeResponse;
import com.activitytracking.dashboard.dto.response.MonthlySummaryResponse;
import com.activitytracking.dashboard.service.impl.DashboardServiceImpl;
import com.activitytracking.masterdata.entity.ActivitySubject;
import com.activitytracking.masterdata.entity.ActivityType;
import com.activitytracking.masterdata.entity.SubjectType;
import com.activitytracking.user.entity.Role;
import com.activitytracking.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private ActivityEntryRepository activityEntryRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User user;
    private ActivityType projectType;
    private ActivityType meetingType;
    private ActivitySubject bmSubject;
    private ActivitySubject licenseSubject;
    private YearMonth month;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Ahmed Yehia");
        user.setEmail("ahmed@myproject.com");
        user.setRole(Role.builder().id(1L).name("EMPLOYEE").build());
        user.setActive(true);

        projectType = ActivityType.builder().id(1L).name("Project").active(true).build();
        meetingType = ActivityType.builder().id(2L).name("Meeting").active(true).build();

        bmSubject = ActivitySubject.builder()
                .id(1L).name("BM Microfocus").subjectType(SubjectType.PROJECT).active(true).build();
        licenseSubject = ActivitySubject.builder()
                .id(2L).name("License Module").subjectType(SubjectType.PROJECT).active(true).build();

        month = YearMonth.of(2026, 8);
    }

    private ActivityEntry buildEntry(LocalDate date, int durationMinutes, ActivityType type, ActivitySubject subject) {
        ActivityEntry entry = new ActivityEntry();
        entry.setUser(user);
        entry.setActivityDate(date);
        entry.setStartTime(LocalTime.of(9, 0));
        entry.setEndTime(LocalTime.of(9, 0).plusMinutes(durationMinutes));
        entry.setDurationMinutes(durationMinutes);
        entry.setActivityType(type);
        entry.setActivitySubject(subject);
        entry.setTaskDescription("Task");
        return entry;
    }

    // ---------- Monthly Summary: Total Days & Total Hours ----------

    @Test
    void getMonthlySummary_shouldCalculateTotalDaysAndMinutes_whenMultipleEntriesAcrossDifferentDays() {
        List<ActivityEntry> entries = List.of(
                buildEntry(LocalDate.of(2026, 8, 5), 120, projectType, bmSubject),
                buildEntry(LocalDate.of(2026, 8, 6), 90, meetingType, licenseSubject),
                buildEntry(LocalDate.of(2026, 8, 7), 60, projectType, bmSubject)
        );

        when(activityEntryRepository.findByUserIdAndActivityDateBetween(
                1L, month.atDay(1), month.atEndOfMonth())).thenReturn(entries);

        MonthlySummaryResponse response = dashboardService.getMonthlySummary(1L, month);

        assertThat(response.getTotalDays()).isEqualTo(3);
        assertThat(response.getTotalHoursMinutes()).isEqualTo(270);
    }

    @Test
    void getMonthlySummary_shouldCountDayOnce_whenMultipleEntriesOnSameDay() {
        List<ActivityEntry> entries = List.of(
                buildEntry(LocalDate.of(2026, 8, 5), 120, projectType, bmSubject),
                buildEntry(LocalDate.of(2026, 8, 5), 60, meetingType, licenseSubject)
        );

        when(activityEntryRepository.findByUserIdAndActivityDateBetween(
                1L, month.atDay(1), month.atEndOfMonth())).thenReturn(entries);

        MonthlySummaryResponse response = dashboardService.getMonthlySummary(1L, month);

        assertThat(response.getTotalDays()).isEqualTo(1);
        assertThat(response.getTotalHoursMinutes()).isEqualTo(180);
    }

    @Test
    void getMonthlySummary_shouldReturnZero_whenNoEntriesInMonth() {
        when(activityEntryRepository.findByUserIdAndActivityDateBetween(
                1L, month.atDay(1), month.atEndOfMonth())).thenReturn(List.of());

        MonthlySummaryResponse response = dashboardService.getMonthlySummary(1L, month);

        assertThat(response.getTotalDays()).isZero();
        assertThat(response.getTotalHoursMinutes()).isZero();
    }

    // ---------- Grouped hours by Activity Type ----------

    @Test
    void getHoursByType_shouldGroupAndSumCorrectly_whenMultipleTypesPresent() {
        List<ActivityEntry> entries = List.of(
                buildEntry(LocalDate.of(2026, 8, 5), 120, projectType, bmSubject),
                buildEntry(LocalDate.of(2026, 8, 6), 60, projectType, bmSubject),
                buildEntry(LocalDate.of(2026, 8, 7), 90, meetingType, licenseSubject)
        );

        when(activityEntryRepository.findByUserIdAndActivityDateBetween(
                1L, month.atDay(1), month.atEndOfMonth())).thenReturn(entries);

        List<HoursByTypeResponse> response = dashboardService.getHoursByType(1L, month);

        assertThat(response).hasSize(2);
        assertThat(response)
                .filteredOn(r -> r.getActivityTypeName().equals("Project"))
                .extracting(HoursByTypeResponse::getHoursMinutes)
                .containsExactly(180L);
        assertThat(response)
                .filteredOn(r -> r.getActivityTypeName().equals("Meeting"))
                .extracting(HoursByTypeResponse::getHoursMinutes)
                .containsExactly(90L);
    }

    @Test
    void getHoursByType_shouldReturnEmptyList_whenNoEntriesInMonth() {
        when(activityEntryRepository.findByUserIdAndActivityDateBetween(
                1L, month.atDay(1), month.atEndOfMonth())).thenReturn(List.of());

        List<HoursByTypeResponse> response = dashboardService.getHoursByType(1L, month);

        assertThat(response).isEmpty();
    }

    // ---------- Grouped hours by Activity Subject ----------

    @Test
    void getHoursBySubject_shouldGroupAndSumCorrectly_whenMultipleSubjectsPresent() {
        List<ActivityEntry> entries = List.of(
                buildEntry(LocalDate.of(2026, 8, 5), 120, projectType, bmSubject),
                buildEntry(LocalDate.of(2026, 8, 6), 30, meetingType, bmSubject),
                buildEntry(LocalDate.of(2026, 8, 7), 90, projectType, licenseSubject)
        );

        when(activityEntryRepository.findByUserIdAndActivityDateBetween(
                1L, month.atDay(1), month.atEndOfMonth())).thenReturn(entries);

        List<HoursBySubjectResponse> response = dashboardService.getHoursBySubject(1L, month);

        assertThat(response).hasSize(2);
        assertThat(response)
                .filteredOn(r -> r.getActivitySubjectName().equals("BM Microfocus"))
                .extracting(HoursBySubjectResponse::getHoursMinutes)
                .containsExactly(150L);
        assertThat(response)
                .filteredOn(r -> r.getActivitySubjectName().equals("License Module"))
                .extracting(HoursBySubjectResponse::getHoursMinutes)
                .containsExactly(90L);
    }

    @Test
    void getHoursBySubject_shouldReturnEmptyList_whenNoEntriesInMonth() {
        when(activityEntryRepository.findByUserIdAndActivityDateBetween(
                1L, month.atDay(1), month.atEndOfMonth())).thenReturn(List.of());

        List<HoursBySubjectResponse> response = dashboardService.getHoursBySubject(1L, month);

        assertThat(response).isEmpty();
    }
}