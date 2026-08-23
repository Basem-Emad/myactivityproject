package com.raya.activitytracking.reporting.excel;

import com.raya.activitytracking.reporting.dto.response.ActivityDetailResponse;
import com.raya.activitytracking.reporting.dto.response.HoursBySubjectResponse;
import com.raya.activitytracking.reporting.dto.response.HoursByTypeResponse;
import com.raya.activitytracking.reporting.dto.response.MonthlyDetailsResponse;
import com.raya.activitytracking.reporting.dto.response.MonthlySummaryResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class ExcelReportGenerator {

    private CellStyle dateStyle;
    private CellStyle timeStyle;
    private CellStyle numericStyle;
    private CellStyle headerStyle;

    public byte[] generate(
            MonthlySummaryResponse summary,
            MonthlyDetailsResponse details,
            List<HoursByTypeResponse> byType,
            List<HoursBySubjectResponse> bySubject) {

        try (Workbook workbook = new XSSFWorkbook()) {

            initializeStyles(workbook);

            buildSummarySheet(workbook, summary);
            buildActivitiesSheet(workbook, details);
            buildByTypeSheet(workbook, byType);
            buildBySubjectSheet(workbook, bySubject);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate Excel workbook", e);
        }
    }

    private void initializeStyles(Workbook workbook) {
        CreationHelper createHelper = workbook.getCreationHelper();

        // Header style
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        headerStyle = workbook.createCellStyle();
        headerStyle.setFont(boldFont);

        // Date style (yyyy-mm-dd format)
        dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

        // Time style (HH:mm format)
        timeStyle = workbook.createCellStyle();
        timeStyle.setDataFormat(createHelper.createDataFormat().getFormat("hh:mm"));

        // Numeric style (standard number format)
        numericStyle = workbook.createCellStyle();
        numericStyle.setDataFormat(createHelper.createDataFormat().getFormat("0"));
    }

    private void buildSummarySheet(Workbook workbook, MonthlySummaryResponse summary) {
        Sheet sheet = workbook.createSheet("Summary");

        Row headerRow = sheet.createRow(0);
        createTextCell(headerRow, 0, "Label", headerStyle);
        createTextCell(headerRow, 1, "Value", headerStyle);

        // Month (text)
        Row row1 = sheet.createRow(1);
        createTextCell(row1, 0, "Month");
        createTextCell(row1, 1, summary.getMonth());

        // Total Days (numeric)
        Row row2 = sheet.createRow(2);
        createTextCell(row2, 0, "Total Days");
        createNumericCell(row2, 1, summary.getTotalDays());

        // Total Duration (formatted text)
        Row row3 = sheet.createRow(3);
        createTextCell(row3, 0, "Total Duration");
        createTextCell(row3, 1, formatMinutes(summary.getTotalDurationMinutes()));

        // Average Duration per Day (formatted text)
        Row row4 = sheet.createRow(4);
        createTextCell(row4, 0, "Average Duration per Day");
        createTextCell(row4, 1, formatMinutes(summary.getAverageDurationMinutes()));

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void buildActivitiesSheet(Workbook workbook, MonthlyDetailsResponse details) {
        Sheet sheet = workbook.createSheet("Activities");

        String[] headers = {"Date", "Start Time", "End Time", "Activity Type",
                "Activity Subject", "Task Description", "Duration"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            createTextCell(headerRow, i, headers[i], headerStyle);
        }

        List<ActivityDetailResponse> activities =
                details.getActivities() != null ? details.getActivities() : List.of();

        int rowIdx = 1;
        for (ActivityDetailResponse activity : activities) {
            Row row = sheet.createRow(rowIdx++);
            
            // Date column - use proper Date cell type
            createDateCell(row, 0, activity.getDate());
            
            // Start Time column - use proper Time cell type
            createTimeCell(row, 1, activity.getStartTime());
            
            // End Time column - use proper Time cell type
            createTimeCell(row, 2, activity.getEndTime());
            
            // Activity Type, Subject, Description - text
            createTextCell(row, 3, activity.getActivityType());
            createTextCell(row, 4, activity.getActivitySubject());
            createTextCell(row, 5, activity.getTaskDescription());
            
            // Duration - formatted text (e.g., "3h 30m")
            createTextCell(row, 6, formatMinutes(activity.getDurationMinutes()));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void buildByTypeSheet(Workbook workbook, List<HoursByTypeResponse> byType) {
        Sheet sheet = workbook.createSheet("By Activity Type");

        Row headerRow = sheet.createRow(0);
        createTextCell(headerRow, 0, "Activity Type", headerStyle);
        createTextCell(headerRow, 1, "Total Duration", headerStyle);

        int rowIdx = 1;
        for (HoursByTypeResponse entry : byType) {
            Row row = sheet.createRow(rowIdx++);
            createTextCell(row, 0, entry.getActivityType());
            createTextCell(row, 1, formatMinutes(entry.getTotalDurationMinutes()));
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void buildBySubjectSheet(Workbook workbook, List<HoursBySubjectResponse> bySubject) {
        Sheet sheet = workbook.createSheet("By Subject");

        Row headerRow = sheet.createRow(0);
        createTextCell(headerRow, 0, "Activity Subject", headerStyle);
        createTextCell(headerRow, 1, "Total Duration", headerStyle);

        int rowIdx = 1;
        for (HoursBySubjectResponse entry : bySubject) {
            Row row = sheet.createRow(rowIdx++);
            createTextCell(row, 0, entry.getActivitySubject());
            createTextCell(row, 1, formatMinutes(entry.getTotalDurationMinutes()));
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private String formatMinutes(long minutes) {
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        return hours + "h " + remainingMinutes + "m";
    }

    // Helper methods for creating typed cells

    /**
     * Creates a text cell with optional style
     */
    private void createTextCell(Row row, int column, String value) {
        createTextCell(row, column, value, null);
    }

    private void createTextCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    /**
     * Creates a numeric cell with proper numeric formatting
     */
    private void createNumericCell(Row row, int column, long value) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(numericStyle);
    }

    /**
     * Creates a date cell with proper date formatting (yyyy-mm-dd)
     */
    private void createDateCell(Row row, int column, LocalDate date) {
        Cell cell = row.createCell(column);
        if (date != null) {
            // Convert LocalDate to Date for Excel
            Date excelDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            cell.setCellValue(excelDate);
            cell.setCellStyle(dateStyle);
        } else {
            cell.setCellValue("");
        }
    }

    /**
     * Creates a time cell with proper time formatting (HH:mm)
     */
    private void createTimeCell(Row row, int column, LocalTime time) {
        Cell cell = row.createCell(column);
        if (time != null) {
            // Convert LocalTime to fraction of day for Excel
            double timeValue = time.toSecondOfDay() / 86400.0;
            cell.setCellValue(timeValue);
            cell.setCellStyle(timeStyle);
        } else {
            cell.setCellValue("");
        }
    }
}
