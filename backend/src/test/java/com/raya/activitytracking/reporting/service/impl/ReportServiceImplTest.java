package com.raya.activitytracking.reporting.service.impl;

import com.raya.activitytracking.activity.entity.ActivityEntry;
import com.raya.activitytracking.activity.repository.ActivityEntryRepository;
import com.raya.activitytracking.masterdata.entity.ActivitySubject;
import com.raya.activitytracking.masterdata.entity.ActivityType;
import com.raya.activitytracking.reporting.dto.response.ActivityDetailResponse;
import com.raya.activitytracking.reporting.dto.response.DailyTrendResponse;
import com.raya.activitytracking.reporting.dto.response.HoursBySubjectResponse;
import com.raya.activitytracking.reporting.dto.response.HoursByTypeResponse;
import com.raya.activitytracking.reporting.dto.response.MonthlyDetailsResponse;
import com.raya.activitytracking.reporting.dto.response.MonthlySummaryResponse;
import com.raya.activitytracking.usermanagement.entity.Role;
import com.raya.activitytracking.usermanagement.entity.User;
import com.raya.activitytracking.usermanagement.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ActivityEntryRepository activityEntryRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // Mock Spring Security context with authenticated user
        Role mockRole = new Role();
        mockRole.setId(1L);
        mockRole.setName("USER");
        mockRole.setPermissions(Collections.emptySet()); // Empty permissions for testing

        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        mockUser.setUserName("testuser");
        mockUser.setEmail("testuser@example.com");
        mockUser.setPassword("encodedPassword");
        mockUser.setRole(mockRole);

        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        // Clean up security context after each test
        SecurityContextHolder.clearContext();
    }

    // ─── MONTHLY SUMMARY TESTS ───────────────────────────────

    @Test
    @DisplayName("getMonthlySummary: calculates totals correctly for month with activities")
    void getMonthlySummary_shouldCalculateCorrectly_whenActivitiesExist() {
        YearMonth month = YearMonth.of(2026, 8);
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        ActivityType projectType = ActivityType.builder().id(1L).name("Project").build();
        ActivitySubject devSubject = ActivitySubject.builder().id(1L).name("Development").build();

        List<ActivityEntry> entries = Arrays.asList(
                createEntry(1L, LocalDate.of(2026, 8, 1), 480, projectType, devSubject),
                createEntry(2L, LocalDate.of(2026, 8, 1), 120, projectType, devSubject),
                createEntry(3L, LocalDate.of(2026, 8, 5), 360, projectType, devSubject),
                createEntry(4L, LocalDate.of(2026, 8, 10), 240, projectType, devSubject)
        );

        when(activityEntryRepository.findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
                TEST_USER_ID, start, end)).thenReturn(entries);

        MonthlySummaryResponse response = reportService.getMonthlySummary(month);

        assertThat(response).isNotNull();
        assertThat(response.getMonth()).isEqualTo("2026-08");
        assertThat(response.getTotalDays()).isEqualTo(3); // 3 distinct dates
        assertThat(response.getTotalDurationMinutes()).isEqualTo(1200); // 480+120+360+240
        assertThat(response.getAverageDurationMinutes()).isEqualTo(400); // 1200/3
    }

    @Test
    @DisplayName("getMonthlySummary: returns zeros for empty month")
    void getMonthlySummary_shouldReturnZeros_whenNoActivities() {
        YearMonth month = YearMonth.of(2026, 9);
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        when(activityEntryRepository.findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
                TEST_USER_ID, start, end)).thenReturn(Collections.emptyList());

        MonthlySummaryResponse response = reportService.getMonthlySummary(month);

        assertThat(response).isNotNull();
        assertThat(response.getMonth()).isEqualTo("2026-09");
        assertThat(response.getTotalDays()).isEqualTo(0);
        assertThat(response.getTotalDurationMinutes()).isEqualTo(0);
        assertThat(response.getAverageDurationMinutes()).isEqualTo(0); // No division by zero
    }

    @Test
    @DisplayName("getMonthlySummary: handles single day with multiple activities")
    void getMonthlySummary_shouldHandleSingleDay_withMultipleActivities() {
        YearMonth month = YearMonth.of(2026, 8);
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        ActivityType projectType = ActivityType.builder().id(1L).name("Project").build();
        ActivitySubject devSubject = ActivitySubject.builder().id(1L).name("Development").build();

        List<ActivityEntry> entries = Arrays.asList(
                createEntry(1L, LocalDate.of(2026, 8, 15), 240, projectType, devSubject),
                createEntry(2L, LocalDate.of(2026, 8, 15), 180, projectType, devSubject),
                createEntry(3L, LocalDate.of(2026, 8, 15), 120, projectType, devSubject)
        );

        when(activityEntryRepository.findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
                TEST_USER_ID, start, end)).thenReturn(entries);

        MonthlySummaryResponse response = reportService.getMonthlySummary(month);

        assertThat(response.getTotalDays()).isEqualTo(1);
        assertThat(response.getTotalDurationMinutes()).isEqualTo(540); // 240+180+120
        assertThat(response.getAverageDurationMinutes()).isEqualTo(540); // All on one day
    }

    // ─── HOURS BY TYPE TESTS ─────────────────────────────────

    @Test
    @DisplayName("getHoursByType: returns aggregated data grouped by activity type")
    void getHoursByType_shouldReturnGroupedData_whenActivitiesExist() {
        YearMonth month = YearMonth.of(2026, 8);
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        List<Object[]> mockResults = Arrays.asList(
                new Object[]{"Meeting", 300L},
                new Object[]{"Project", 1200L},
                new Object[]{"Training", 180L}
        );

        when(activityEntryRepository.sumDurationByTypeBetween(TEST_USER_ID, start, end))
                .thenReturn(mockResults);

        List<HoursByTypeResponse> response = reportService.getHoursByType(month);

        assertThat(response).hasSize(3);
        assertThat(response.get(0).getActivityType()).isEqualTo("Meeting");
        assertThat(response.get(0).getTotalDurationMinutes()).isEqualTo(300L);
        assertThat(response.get(1).getActivityType()).isEqualTo("Project");
        assertThat(response.get(1).getTotalDurationMinutes()).isEqualTo(1200L);
        assertThat(response.get(2).getActivityType()).isEqualTo("Training");
        assertThat(response.get(2).getTotalDurationMinutes()).isEqualTo(180L);
    }

    @Test
    @DisplayName("getHoursByType: returns empty list for month with no activities")
    void getHoursByType_shouldReturnEmptyList_whenNoActivities() {
        YearMonth month = YearMonth.of(2026, 9);
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        when(activityEntryRepository.sumDurationByTypeBetween(TEST_USER_ID, start, end))
                .thenReturn(Collections.emptyList());

        List<HoursByTypeResponse> response = reportService.getHoursByType(month);

        assertThat(response).isEmpty();
    }

    // ─── HOURS BY SUBJECT TESTS ──────────────────────────────

    @Test
    @DisplayName("getHoursBySubject: returns aggregated data grouped by activity subject")
    void getHoursBySubject_shouldReturnGroupedData_whenActivitiesExist() {
        YearMonth month = YearMonth.of(2026, 8);
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        List<Object[]> mockResults = Arrays.asList(
                new Object[]{"Backend Development", 900L},
                new Object[]{"Code Review", 240L},
                new Object[]{"Frontend Development", 600L}
        );

        when(activityEntryRepository.sumDurationBySubjectBetween(TEST_USER_ID, start, end))
                .thenReturn(mockResults);

        List<HoursBySubjectResponse> response = reportService.getHoursBySubject(month);

        assertThat(response).hasSize(3);
        assertThat(response.get(0).getActivitySubject()).isEqualTo("Backend Development");
        assertThat(response.get(0).getTotalDurationMinutes()).isEqualTo(900L);
        assertThat(response.get(1).getActivitySubject()).isEqualTo("Code Review");
        assertThat(response.get(1).getTotalDurationMinutes()).isEqualTo(240L);
        assertThat(response.get(2).getActivitySubject()).isEqualTo("Frontend Development");
        assertThat(response.get(2).getTotalDurationMinutes()).isEqualTo(600L);
    }

    @Test
    @DisplayName("getHoursBySubject: returns empty list for month with no activities")
    void getHoursBySubject_shouldReturnEmptyList_whenNoActivities() {
        YearMonth month = YearMonth.of(2026, 9);
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        when(activityEntryRepository.sumDurationBySubjectBetween(TEST_USER_ID, start, end))
                .thenReturn(Collections.emptyList());

        List<HoursBySubjectResponse> response = reportService.getHoursBySubject(month);

        assertThat(response).isEmpty();
    }

    // ─── DAILY TREND TESTS ───────────────────────────────────

    @Test
    @DisplayName("getDailyTrend: groups activities by date and sums durations")
    void getDailyTrend_shouldGroupByDate_andSumDurations() {
        YearMonth month = YearMonth.of(2026, 8);
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        ActivityType projectType = ActivityType.builder().id(1L).name("Project").build();
        ActivitySubject devSubject = ActivitySubject.builder().id(1L).name("Development").build();

        List<ActivityEntry> entries = Arrays.asList(
                createEntry(1L, LocalDate.of(2026, 8, 1), 240, projectType, devSubject),
                createEntry(2L, LocalDate.of(2026, 8, 1), 180, projectType, devSubject),
                createEntry(3L, LocalDate.of(2026, 8, 5), 360, projectType, devSubject),
                createEntry(4L, LocalDate.of(2026, 8, 10), 480, projectType, devSubject)
        );

        when(activityEntryRepository.findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
                TEST_USER_ID, start, end)).thenReturn(entries);

        List<DailyTrendResponse> response = reportService.getDailyTrend(month);

        assertThat(response).hasSize(3);
        
        // Verify sorted by date
        assertThat(response.get(0).getDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(response.get(0).getTotalDurationMinutes()).isEqualTo(420); // 240+180
        
        assertThat(response.get(1).getDate()).isEqualTo(LocalDate.of(2026, 8, 5));
        assertThat(response.get(1).getTotalDurationMinutes()).isEqualTo(360);
        
        assertThat(response.get(2).getDate()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(response.get(2).getTotalDurationMinutes()).isEqualTo(480);
    }

    @Test
    @DisplayName("getDailyTrend: returns empty list for month with no activities")
    void getDailyTrend_shouldReturnEmptyList_whenNoActivities() {
        YearMonth month = YearMonth.of(2026, 9);
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        when(activityEntryRepository.findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
                TEST_USER_ID, start, end)).thenReturn(Collections.emptyList());

        List<DailyTrendResponse> response = reportService.getDailyTrend(month);

        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("getDailyTrend: maintains chronological order")
    void getDailyTrend_shouldMaintainChronologicalOrder() {
        YearMonth month = YearMonth.of(2026, 8);
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        ActivityType projectType = ActivityType.builder().id(1L).name("Project").build();
        ActivitySubject devSubject = ActivitySubject.builder().id(1L).name("Development").build();

        // Add dates out of order
        List<ActivityEntry> entries = Arrays.asList(
                createEntry(1L, LocalDate.of(2026, 8, 20), 300, projectType, devSubject),
                createEntry(2L, LocalDate.of(2026, 8, 5), 200, projectType, devSubject),
                createEntry(3L, LocalDate.of(2026, 8, 15), 400, projectType, devSubject)
        );

        when(activityEntryRepository.findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
                TEST_USER_ID, start, end)).thenReturn(entries);

        List<DailyTrendResponse> response = reportService.getDailyTrend(month);

        // Should be sorted chronologically
        assertThat(response.get(0).getDate()).isEqualTo(LocalDate.of(2026, 8, 5));
        assertThat(response.get(1).getDate()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(response.get(2).getDate()).isEqualTo(LocalDate.of(2026, 8, 20));
    }

    // ─── MONTHLY DETAILS TESTS ───────────────────────────────

    @Test
    @DisplayName("getMonthlyDetails: maps all activity fields correctly")
    void getMonthlyDetails_shouldMapAllFields_correctly() {
        YearMonth month = YearMonth.of(2026, 8);
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        ActivityType projectType = ActivityType.builder().id(1L).name("Project").build();
        ActivitySubject devSubject = ActivitySubject.builder().id(2L).name("Backend Dev").build();

        ActivityEntry entry = ActivityEntry.builder()
                .id(100L)
                .userId(TEST_USER_ID)
                .date(LocalDate.of(2026, 8, 15))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 30))
                .durationMinutes(150)
                .activityType(projectType)
                .activitySubject(devSubject)
                .taskDescription("Implemented reporting module")
                .build();

        when(activityEntryRepository.findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
                TEST_USER_ID, start, end)).thenReturn(Collections.singletonList(entry));

        MonthlyDetailsResponse response = reportService.getMonthlyDetails(month);

        assertThat(response).isNotNull();
        assertThat(response.getMonth()).isEqualTo("2026-08");
        assertThat(response.getActivities()).hasSize(1);

        ActivityDetailResponse detail = response.getActivities().get(0);
        assertThat(detail.getId()).isEqualTo(100L);
        assertThat(detail.getDate()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(detail.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(detail.getEndTime()).isEqualTo(LocalTime.of(11, 30));
        assertThat(detail.getActivityType()).isEqualTo("Project");
        assertThat(detail.getActivitySubject()).isEqualTo("Backend Dev");
        assertThat(detail.getTaskDescription()).isEqualTo("Implemented reporting module");
        assertThat(detail.getDurationMinutes()).isEqualTo(150);
    }

    @Test
    @DisplayName("getMonthlyDetails: returns empty activities list for month with no activities")
    void getMonthlyDetails_shouldReturnEmptyList_whenNoActivities() {
        YearMonth month = YearMonth.of(2026, 9);
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        when(activityEntryRepository.findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
                TEST_USER_ID, start, end)).thenReturn(Collections.emptyList());

        MonthlyDetailsResponse response = reportService.getMonthlyDetails(month);

        assertThat(response).isNotNull();
        assertThat(response.getMonth()).isEqualTo("2026-09");
        assertThat(response.getActivities()).isEmpty();
    }

    @Test
    @DisplayName("getMonthlyDetails: handles multiple activities correctly")
    void getMonthlyDetails_shouldHandleMultipleActivities() {
        YearMonth month = YearMonth.of(2026, 8);
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);

        ActivityType projectType = ActivityType.builder().id(1L).name("Project").build();
        ActivityType meetingType = ActivityType.builder().id(2L).name("Meeting").build();
        ActivitySubject devSubject = ActivitySubject.builder().id(1L).name("Development").build();

        List<ActivityEntry> entries = Arrays.asList(
                createEntry(1L, LocalDate.of(2026, 8, 1), 480, projectType, devSubject),
                createEntry(2L, LocalDate.of(2026, 8, 1), 60, meetingType, devSubject),
                createEntry(3L, LocalDate.of(2026, 8, 5), 360, projectType, devSubject)
        );

        when(activityEntryRepository.findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
                TEST_USER_ID, start, end)).thenReturn(entries);

        MonthlyDetailsResponse response = reportService.getMonthlyDetails(month);

        assertThat(response.getActivities()).hasSize(3);
        assertThat(response.getActivities().get(0).getId()).isEqualTo(1L);
        assertThat(response.getActivities().get(1).getId()).isEqualTo(2L);
        assertThat(response.getActivities().get(2).getId()).isEqualTo(3L);
    }

    // ─── DATE RANGE TESTS ────────────────────────────────────

    @Test
    @DisplayName("All methods: correctly calculate month boundaries")
    void allMethods_shouldUseCorrectDateRange_forFebruary() {
        YearMonth month = YearMonth.of(2026, 2);
        LocalDate expectedStart = LocalDate.of(2026, 2, 1);
        LocalDate expectedEnd = LocalDate.of(2026, 2, 28); // Non-leap year

        when(activityEntryRepository.findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
                eq(TEST_USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        reportService.getMonthlySummary(month);

        verify(activityEntryRepository).findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
                TEST_USER_ID, expectedStart, expectedEnd);
    }

    @Test
    @DisplayName("All methods: correctly handle 31-day months")
    void allMethods_shouldUseCorrectDateRange_forAugust() {
        YearMonth month = YearMonth.of(2026, 8);
        LocalDate expectedStart = LocalDate.of(2026, 8, 1);
        LocalDate expectedEnd = LocalDate.of(2026, 8, 31);

        when(activityEntryRepository.sumDurationByTypeBetween(
                eq(TEST_USER_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        reportService.getHoursByType(month);

        verify(activityEntryRepository).sumDurationByTypeBetween(
                TEST_USER_ID, expectedStart, expectedEnd);
    }

    // ─── HELPER METHODS ──────────────────────────────────────

    private ActivityEntry createEntry(Long id, LocalDate date, int durationMinutes,
                                      ActivityType type, ActivitySubject subject) {
        return ActivityEntry.builder()
                .id(id)
                .userId(TEST_USER_ID)
                .date(date)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 0).plusMinutes(durationMinutes))
                .durationMinutes(durationMinutes)
                .activityType(type)
                .activitySubject(subject)
                .taskDescription("Test activity")
                .build();
    }
}
