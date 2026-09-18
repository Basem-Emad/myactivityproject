package com.activitytracking.dashboard.service.impl;

import com.activitytracking.activity.entity.ActivityEntry;
import com.activitytracking.activity.repository.ActivityEntryRepository;
import com.activitytracking.dashboard.dto.response.HoursBySubjectResponse;
import com.activitytracking.dashboard.dto.response.HoursByTypeResponse;
import com.activitytracking.dashboard.dto.response.MonthlySummaryResponse;
import com.activitytracking.dashboard.service.DashboardService;
import com.activitytracking.masterdata.entity.ActivitySubject;
import com.activitytracking.masterdata.entity.ActivityType;
import com.activitytracking.masterdata.entity.SubjectType;
import com.activitytracking.user.entity.Role;
import com.activitytracking.user.entity.User;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelExportServiceImplTest {

    @Mock
    private ActivityEntryRepository activityEntryRepository;

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private ExcelExportServiceImpl excelExportService;

    private final YearMonth month = YearMonth.of(2026, 8);

    @Test
    void exportMonthlyReport_shouldGenerateAllFourSheets_withExpectedData() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Ahmed Yehia");
        user.setRole(Role.builder().id(1L).name("EMPLOYEE").permissions(Set.of()).build());

        ActivityType type = ActivityType.builder().id(1L).name("Project").active(true).build();
        ActivitySubject subject = ActivitySubject.builder()
                .id(1L).name("BM Microfocus").subjectType(SubjectType.PROJECT).active(true).build();

        ActivityEntry entry = new ActivityEntry();
        entry.setId(1L);
        entry.setUser(user);
        entry.setActivityType(type);
        entry.setActivitySubject(subject);
        entry.setActivityDate(LocalDate.of(2026, 8, 3));
        entry.setStartTime(LocalTime.of(9, 0));
        entry.setEndTime(LocalTime.of(11, 0));
        entry.setDurationMinutes(120);
        entry.setTaskDescription("Backend API work");

        when(activityEntryRepository.findByUserIdAndActivityDateBetween(1L, month.atDay(1), month.atEndOfMonth()))
                .thenReturn(List.of(entry));
        when(dashboardService.getMonthlySummary(1L, month))
                .thenReturn(new MonthlySummaryResponse(20, 4800)); // 80 hours
        when(dashboardService.getHoursByType(1L, month))
                .thenReturn(List.of(new HoursByTypeResponse("Project", 4800)));
        when(dashboardService.getHoursBySubject(1L, month))
                .thenReturn(List.of(new HoursBySubjectResponse("BM Microfocus", 4800)));

        ByteArrayOutputStream output = excelExportService.exportMonthlyReport(1L, month);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(output.toByteArray()))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(4);
            assertThat(sheetNames(workbook)).containsExactly(
                    "Summary", "Activities", "By Activity Type", "By Subject");

            Sheet summarySheet = workbook.getSheet("Summary");
            assertThat(summarySheet.getRow(2).getCell(1).getNumericCellValue()).isEqualTo(20);
            assertThat(summarySheet.getRow(3).getCell(1).getNumericCellValue()).isEqualTo(80.0);

            Sheet activitiesSheet = workbook.getSheet("Activities");
            Row activityRow = activitiesSheet.getRow(1);
            assertThat(activityRow.getCell(0).getStringCellValue()).isEqualTo("2026-08-03");
            assertThat(activityRow.getCell(3).getNumericCellValue()).isEqualTo(120);
            assertThat(activityRow.getCell(4).getStringCellValue()).isEqualTo("Project");

            Sheet byTypeSheet = workbook.getSheet("By Activity Type");
            assertThat(byTypeSheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Project");
            assertThat(byTypeSheet.getRow(1).getCell(1).getNumericCellValue()).isEqualTo(80.0);

            Sheet bySubjectSheet = workbook.getSheet("By Subject");
            assertThat(bySubjectSheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("BM Microfocus");
        }
    }

    @Test
    void exportMonthlyReport_shouldProduceEmptySheets_whenNoActivitiesInMonth() throws Exception {
        when(activityEntryRepository.findByUserIdAndActivityDateBetween(1L, month.atDay(1), month.atEndOfMonth()))
                .thenReturn(List.of());
        when(dashboardService.getMonthlySummary(1L, month))
                .thenReturn(new MonthlySummaryResponse(0, 0));
        when(dashboardService.getHoursByType(1L, month)).thenReturn(List.of());
        when(dashboardService.getHoursBySubject(1L, month)).thenReturn(List.of());

        ByteArrayOutputStream output = excelExportService.exportMonthlyReport(1L, month);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(output.toByteArray()))) {
            Sheet activitiesSheet = workbook.getSheet("Activities");
            assertThat(activitiesSheet.getPhysicalNumberOfRows()).isEqualTo(1); // header row only
        }
    }

    private List<String> sheetNames(Workbook workbook) {
        List<String> names = new java.util.ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            names.add(workbook.getSheetName(i));
        }
        return names;
    }
}
