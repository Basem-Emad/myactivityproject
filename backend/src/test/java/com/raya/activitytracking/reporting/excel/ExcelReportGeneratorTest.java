package com.raya.activitytracking.reporting.excel;

import com.raya.activitytracking.reporting.dto.response.ActivityDetailResponse;
import com.raya.activitytracking.reporting.dto.response.HoursBySubjectResponse;
import com.raya.activitytracking.reporting.dto.response.HoursByTypeResponse;
import com.raya.activitytracking.reporting.dto.response.MonthlyDetailsResponse;
import com.raya.activitytracking.reporting.dto.response.MonthlySummaryResponse;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelReportGeneratorTest {

    private ExcelReportGenerator generator;

    private MonthlySummaryResponse summary;
    private MonthlyDetailsResponse details;
    private List<HoursByTypeResponse> byType;
    private List<HoursBySubjectResponse> bySubject;

    @BeforeEach
    void setUp() {
        generator = new ExcelReportGenerator();

        summary = MonthlySummaryResponse.builder()
                .month("2026-08")
                .totalDays(20)
                .totalDurationMinutes(9630)
                .averageDurationMinutes(482)
                .build();

        ActivityDetailResponse activity = ActivityDetailResponse.builder()
                .id(1L)
                .date(LocalDate.of(2026, 8, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 30))
                .activityType("Project")
                .activitySubject("BM Microfocus")
                .taskDescription("Implemented reporting API")
                .durationMinutes(150)
                .build();

        details = MonthlyDetailsResponse.builder()
                .month("2026-08")
                .activities(List.of(activity))
                .build();

        byType = List.of(
                HoursByTypeResponse.builder()
                        .activityType("Project")
                        .totalDurationMinutes(3600)
                        .build(),
                HoursByTypeResponse.builder()
                        .activityType("Meeting")
                        .totalDurationMinutes(1800)
                        .build()
        );

        bySubject = List.of(
                HoursBySubjectResponse.builder()
                        .activitySubject("BM Microfocus")
                        .totalDurationMinutes(3900)
                        .build(),
                HoursBySubjectResponse.builder()
                        .activitySubject("Internal")
                        .totalDurationMinutes(1500)
                        .build()
        );
    }

    @Test
    void generate_shouldReturnValidXlsxWorkbook() throws IOException {
        byte[] result = generator.generate(summary, details, byType, bySubject);

        assertThat(result).isNotNull().isNotEmpty();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertThat(workbook).isNotNull();
        }
    }

    @Test
    void generate_shouldContainFourSheetsWithCorrectNames() throws IOException {
        byte[] result = generator.generate(summary, details, byType, bySubject);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(4);
            assertThat(workbook.getSheetName(0)).isEqualTo("Summary");
            assertThat(workbook.getSheetName(1)).isEqualTo("Activities");
            assertThat(workbook.getSheetName(2)).isEqualTo("By Activity Type");
            assertThat(workbook.getSheetName(3)).isEqualTo("By Subject");
        }
    }

    @Test
    void generate_summarySheet_shouldContainCorrectValues() throws IOException {
        byte[] result = generator.generate(summary, details, byType, bySubject);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Summary");

            assertThat(cellValue(sheet, 1, 0)).isEqualTo("Month");
            assertThat(cellValue(sheet, 1, 1)).isEqualTo("2026-08");

            assertThat(cellValue(sheet, 2, 0)).isEqualTo("Total Days");
            assertThat(cellValue(sheet, 2, 1)).isEqualTo("20");

            assertThat(cellValue(sheet, 3, 0)).isEqualTo("Total Duration");
            assertThat(cellValue(sheet, 3, 1)).isEqualTo("160h 30m");

            assertThat(cellValue(sheet, 4, 0)).isEqualTo("Average Duration per Day");
            assertThat(cellValue(sheet, 4, 1)).isEqualTo("8h 2m");
        }
    }

    @Test
    void generate_activitiesSheet_shouldContainHeaderRow() throws IOException {
        byte[] result = generator.generate(summary, details, byType, bySubject);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Activities");
            Row header = sheet.getRow(0);

            assertThat(cellValue(header, 0)).isEqualTo("Date");
            assertThat(cellValue(header, 1)).isEqualTo("Start Time");
            assertThat(cellValue(header, 2)).isEqualTo("End Time");
            assertThat(cellValue(header, 3)).isEqualTo("Activity Type");
            assertThat(cellValue(header, 4)).isEqualTo("Activity Subject");
            assertThat(cellValue(header, 5)).isEqualTo("Task Description");
            assertThat(cellValue(header, 6)).isEqualTo("Duration");
        }
    }

    @Test
    void generate_activitiesSheet_shouldContainActivityDataRow() throws IOException {
        byte[] result = generator.generate(summary, details, byType, bySubject);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Activities");
            Row dataRow = sheet.getRow(1);

            assertThat(cellValue(dataRow, 0)).isEqualTo("2026-08-01");
            assertThat(cellValue(dataRow, 1)).isEqualTo("09:00");
            assertThat(cellValue(dataRow, 2)).isEqualTo("11:30");
            assertThat(cellValue(dataRow, 3)).isEqualTo("Project");
            assertThat(cellValue(dataRow, 4)).isEqualTo("BM Microfocus");
            assertThat(cellValue(dataRow, 5)).isEqualTo("Implemented reporting API");
            assertThat(cellValue(dataRow, 6)).isEqualTo("2h 30m");
        }
    }

    @Test
    void generate_byTypeSheet_shouldContainCorrectValues() throws IOException {
        byte[] result = generator.generate(summary, details, byType, bySubject);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("By Activity Type");

            assertThat(cellValue(sheet, 0, 0)).isEqualTo("Activity Type");
            assertThat(cellValue(sheet, 0, 1)).isEqualTo("Total Duration");

            assertThat(cellValue(sheet, 1, 0)).isEqualTo("Project");
            assertThat(cellValue(sheet, 1, 1)).isEqualTo("60h 0m");

            assertThat(cellValue(sheet, 2, 0)).isEqualTo("Meeting");
            assertThat(cellValue(sheet, 2, 1)).isEqualTo("30h 0m");
        }
    }

    @Test
    void generate_bySubjectSheet_shouldContainCorrectValues() throws IOException {
        byte[] result = generator.generate(summary, details, byType, bySubject);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("By Subject");

            assertThat(cellValue(sheet, 0, 0)).isEqualTo("Activity Subject");
            assertThat(cellValue(sheet, 0, 1)).isEqualTo("Total Duration");

            assertThat(cellValue(sheet, 1, 0)).isEqualTo("BM Microfocus");
            assertThat(cellValue(sheet, 1, 1)).isEqualTo("65h 0m");

            assertThat(cellValue(sheet, 2, 0)).isEqualTo("Internal");
            assertThat(cellValue(sheet, 2, 1)).isEqualTo("25h 0m");
        }
    }

    @Test
    void generate_emptyLists_shouldProduceValidWorkbookWithHeadersOnly() throws IOException {
        MonthlyDetailsResponse emptyDetails = MonthlyDetailsResponse.builder()
                .month("2026-08")
                .activities(List.of())
                .build();

        byte[] result = generator.generate(summary, emptyDetails, List.of(), List.of());

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(4);

            Sheet activities = workbook.getSheet("Activities");
            assertThat(activities.getRow(0)).isNotNull();
            assertThat(activities.getRow(1)).isNull();

            Sheet byTypeSheet = workbook.getSheet("By Activity Type");
            assertThat(byTypeSheet.getRow(0)).isNotNull();
            assertThat(byTypeSheet.getRow(1)).isNull();

            Sheet bySubjectSheet = workbook.getSheet("By Subject");
            assertThat(bySubjectSheet.getRow(0)).isNotNull();
            assertThat(bySubjectSheet.getRow(1)).isNull();
        }
    }

    @Test
    void generate_durationFormatting_shouldFormatMinutesCorrectly() throws IOException {
        int[][] testCases = {
                {0, 0, 0},
                {30, 0, 30},
                {60, 1, 0},
                {90, 1, 30},
                {125, 2, 5}
        };

        for (int[] testCase : testCases) {
            int minutes = testCase[0];
            int expectedHours = testCase[1];
            int expectedMinutes = testCase[2];

            byte[] result = generator.generate(summary, detailsWithDuration(minutes), List.of(), List.of());

            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
                Sheet sheet = workbook.getSheet("Activities");
                String expected = expectedHours + "h " + expectedMinutes + "m";
                assertThat(cellValue(sheet, 1, 6)).isEqualTo(expected);
            }
        }
    }

    private String cellValue(Sheet sheet, int rowIdx, int colIdx) {
        return cellValue(sheet.getRow(rowIdx), colIdx);
    }

    private String cellValue(Row row, int colIdx) {
        return new DataFormatter().formatCellValue(row.getCell(colIdx));
    }

    private MonthlyDetailsResponse detailsWithDuration(long durationMinutes) {
        ActivityDetailResponse activity = ActivityDetailResponse.builder()
                .id(1L)
                .date(LocalDate.of(2026, 8, 1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .activityType("Project")
                .activitySubject("BM Microfocus")
                .taskDescription("Test")
                .durationMinutes(durationMinutes)
                .build();
        return MonthlyDetailsResponse.builder()
                .month("2026-08")
                .activities(List.of(activity))
                .build();
    }
}
