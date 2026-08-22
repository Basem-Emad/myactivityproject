package com.raya.activitytracking.reporting.controller;

import com.raya.activitytracking.reporting.dto.response.DailyTrendResponse;
import com.raya.activitytracking.reporting.dto.response.HoursBySubjectResponse;
import com.raya.activitytracking.reporting.dto.response.HoursByTypeResponse;
import com.raya.activitytracking.reporting.dto.response.MonthlyDetailsResponse;
import com.raya.activitytracking.reporting.dto.response.MonthlySummaryResponse;
import com.raya.activitytracking.reporting.excel.ExcelReportGenerator;
import com.raya.activitytracking.reporting.service.ReportService;
import com.raya.activitytracking.reporting.util.ReportingMonthParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reporting", description = "Monthly reports, dashboards, and Excel export")
public class ReportController {

    private final ReportService reportService;
    private final ExcelReportGenerator excelReportGenerator;

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly summary",
            description = "Returns total days, total duration, and average duration for the specified month")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @Parameter(description = "Month in YYYY-MM format (e.g. 2026-08)", example = "2026-08")
            @RequestParam String month) {

        YearMonth yearMonth = ReportingMonthParser.parse(month);
        MonthlySummaryResponse summary = reportService.getMonthlySummary(yearMonth);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/hours-by-type")
    @Operation(summary = "Get hours grouped by activity type",
            description = "Returns total duration for each activity type in the specified month")
    public ResponseEntity<List<HoursByTypeResponse>> getHoursByType(
            @Parameter(description = "Month in YYYY-MM format (e.g. 2026-08)", example = "2026-08")
            @RequestParam String month) {

        YearMonth yearMonth = ReportingMonthParser.parse(month);
        List<HoursByTypeResponse> result = reportService.getHoursByType(yearMonth);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/hours-by-subject")
    @Operation(summary = "Get hours grouped by activity subject",
            description = "Returns total duration for each activity subject in the specified month")
    public ResponseEntity<List<HoursBySubjectResponse>> getHoursBySubject(
            @Parameter(description = "Month in YYYY-MM format (e.g. 2026-08)", example = "2026-08")
            @RequestParam String month) {

        YearMonth yearMonth = ReportingMonthParser.parse(month);
        List<HoursBySubjectResponse> result = reportService.getHoursBySubject(yearMonth);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/daily-trend")
    @Operation(summary = "Get daily trend data",
            description = "Returns per-day duration totals for the specified month, used for trend charts")
    public ResponseEntity<List<DailyTrendResponse>> getDailyTrend(
            @Parameter(description = "Month in YYYY-MM format (e.g. 2026-08)", example = "2026-08")
            @RequestParam String month) {

        YearMonth yearMonth = ReportingMonthParser.parse(month);
        List<DailyTrendResponse> result = reportService.getDailyTrend(yearMonth);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/monthly-details")
    @Operation(summary = "Get full activity list for the month",
            description = "Returns all activity entries for the specified month, sorted by date and time")
    public ResponseEntity<MonthlyDetailsResponse> getMonthlyDetails(
            @Parameter(description = "Month in YYYY-MM format (e.g. 2026-08)", example = "2026-08")
            @RequestParam String month) {

        YearMonth yearMonth = ReportingMonthParser.parse(month);
        MonthlyDetailsResponse details = reportService.getMonthlyDetails(yearMonth);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Export monthly report to Excel",
            description = "Downloads an Excel workbook with the full monthly report. Returns 204 No Content if the month has no activities.")
    public ResponseEntity<byte[]> exportToExcel(
            @Parameter(description = "Month in YYYY-MM format (e.g. 2026-08)", example = "2026-08")
            @RequestParam String month) {

        YearMonth yearMonth = ReportingMonthParser.parse(month);

        // Get summary to check if there's any data
        MonthlySummaryResponse summary = reportService.getMonthlySummary(yearMonth);

        // Return 204 No Content if no activities exist
        if (summary.getTotalDays() == 0) {
            return ResponseEntity.noContent().build();
        }

        // Fetch all data for Excel generation
        List<HoursByTypeResponse> hoursByType = reportService.getHoursByType(yearMonth);
        List<HoursBySubjectResponse> hoursBySubject = reportService.getHoursBySubject(yearMonth);
        MonthlyDetailsResponse details = reportService.getMonthlyDetails(yearMonth);

        // Generate Excel workbook
        byte[] excelBytes = excelReportGenerator.generate(summary, details, hoursByType, hoursBySubject);

        // Build filename: "August-2026-Activity-Report.xlsx"
        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String filename = monthName + "-" + yearMonth.getYear() + "-Activity-Report.xlsx";

        // Set response headers for file download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excelBytes.length);

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}
