package com.activitytracking.dashboard.service.impl;

import com.activitytracking.activity.entity.ActivityEntry;
import com.activitytracking.activity.repository.ActivityEntryRepository;
import com.activitytracking.dashboard.dto.response.HoursBySubjectResponse;
import com.activitytracking.dashboard.dto.response.HoursByTypeResponse;
import com.activitytracking.dashboard.dto.response.MonthlySummaryResponse;
import com.activitytracking.dashboard.service.DashboardService;
import com.activitytracking.dashboard.service.ExcelExportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ExcelExportServiceImpl implements ExcelExportService {

    private final ActivityEntryRepository activityEntryRepository;
    private final DashboardService dashboardService;

    public ExcelExportServiceImpl(ActivityEntryRepository activityEntryRepository,
                                   DashboardService dashboardService) {
        this.activityEntryRepository = activityEntryRepository;
        this.dashboardService = dashboardService;
    }

    @Override
    public ByteArrayOutputStream exportMonthlyReport(Long userId, YearMonth month) {

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        List<ActivityEntry> entries =
                activityEntryRepository.findByUserIdAndActivityDateBetween(userId, startDate, endDate);

        MonthlySummaryResponse summary = dashboardService.getMonthlySummary(userId, month);
        List<HoursByTypeResponse> byType = dashboardService.getHoursByType(userId, month);
        List<HoursBySubjectResponse> bySubject = dashboardService.getHoursBySubject(userId, month);

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);

            buildSummarySheet(workbook, headerStyle, month, summary);
            buildActivitiesSheet(workbook, headerStyle, entries);
            buildByTypeSheet(workbook, headerStyle, byType);
            buildBySubjectSheet(workbook, headerStyle, bySubject);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream;

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate Excel report", e);
        }
    }

    private void buildSummarySheet(
            Workbook workbook, CellStyle headerStyle, YearMonth month, MonthlySummaryResponse summary) {

        Sheet sheet = workbook.createSheet("Summary");

        writeHeaderRow(sheet, headerStyle, "Metric", "Value");

        Row monthRow = sheet.createRow(1);
        monthRow.createCell(0).setCellValue("Month");
        monthRow.createCell(1).setCellValue(month.toString());

        Row daysRow = sheet.createRow(2);
        daysRow.createCell(0).setCellValue("Total Days");
        daysRow.createCell(1).setCellValue(summary.getTotalDays());

        Row hoursRow = sheet.createRow(3);
        hoursRow.createCell(0).setCellValue("Total Hours");
        hoursRow.createCell(1).setCellValue(toDecimalHours(summary.getTotalHoursMinutes()));

        autoSizeColumns(sheet, 2);
    }

    private void buildActivitiesSheet(Workbook workbook, CellStyle headerStyle, List<ActivityEntry> entries) {
        Sheet sheet = workbook.createSheet("Activities");

        String[] columns = {"Date", "Start Time", "End Time", "Duration (min)",
                "Activity Type", "Activity Subject", "Task Description"};
        writeHeaderRow(sheet, headerStyle, columns);

        int rowIndex = 1;
        for (ActivityEntry entry : entries) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(entry.getActivityDate().toString());
            row.createCell(1).setCellValue(entry.getStartTime().toString());
            row.createCell(2).setCellValue(entry.getEndTime().toString());
            row.createCell(3).setCellValue(entry.getDurationMinutes());
            row.createCell(4).setCellValue(entry.getActivityType().getName());
            row.createCell(5).setCellValue(entry.getActivitySubject().getName());
            row.createCell(6).setCellValue(entry.getTaskDescription());
        }

        autoSizeColumns(sheet, columns.length);
    }

    private void buildByTypeSheet(Workbook workbook, CellStyle headerStyle, List<HoursByTypeResponse> byType) {
        Sheet sheet = workbook.createSheet("By Activity Type");

        writeHeaderRow(sheet, headerStyle, "Activity Type", "Hours");

        int rowIndex = 1;
        for (HoursByTypeResponse item : byType) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(item.getActivityTypeName());
            row.createCell(1).setCellValue(toDecimalHours(item.getHoursMinutes()));
        }

        autoSizeColumns(sheet, 2);
    }

    private void buildBySubjectSheet(Workbook workbook, CellStyle headerStyle, List<HoursBySubjectResponse> bySubject) {
        Sheet sheet = workbook.createSheet("By Subject");

        writeHeaderRow(sheet, headerStyle, "Activity Subject", "Hours");

        int rowIndex = 1;
        for (HoursBySubjectResponse item : bySubject) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(item.getActivitySubjectName());
            row.createCell(1).setCellValue(toDecimalHours(item.getHoursMinutes()));
        }

        autoSizeColumns(sheet, 2);
    }

    private void writeHeaderRow(Sheet sheet, CellStyle headerStyle, String... columns) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(boldFont);
        return style;
    }

    private double toDecimalHours(long minutes) {
        return Math.round((minutes / 60.0) * 100) / 100.0;
    }
}
