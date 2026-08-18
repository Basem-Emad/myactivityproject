package com.raya.activitytracking.reporting.excel;

import com.raya.activitytracking.reporting.dto.response.ActivityDetailResponse;
import com.raya.activitytracking.reporting.dto.response.HoursBySubjectResponse;
import com.raya.activitytracking.reporting.dto.response.HoursByTypeResponse;
import com.raya.activitytracking.reporting.dto.response.MonthlyDetailsResponse;
import com.raya.activitytracking.reporting.dto.response.MonthlySummaryResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelReportGenerator {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public byte[] generate(
            MonthlySummaryResponse summary,
            MonthlyDetailsResponse details,
            List<HoursByTypeResponse> byType,
            List<HoursBySubjectResponse> bySubject) {

        try (Workbook workbook = new XSSFWorkbook()) {

            CellStyle headerStyle = createHeaderStyle(workbook);

            buildSummarySheet(workbook, headerStyle, summary);
            buildActivitiesSheet(workbook, headerStyle, details);
            buildByTypeSheet(workbook, headerStyle, byType);
            buildBySubjectSheet(workbook, headerStyle, bySubject);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate Excel workbook", e);
        }
    }

    private void buildSummarySheet(Workbook workbook, CellStyle headerStyle,
                                   MonthlySummaryResponse summary) {
        Sheet sheet = workbook.createSheet("Summary");

        String[] labels = {"Month", "Total Days", "Total Duration", "Average Duration per Day"};
        String[] values = {
                summary.getMonth(),
                String.valueOf(summary.getTotalDays()),
                formatMinutes(summary.getTotalDurationMinutes()),
                formatMinutes(summary.getAverageDurationMinutes())
        };

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Label", headerStyle);
        createCell(headerRow, 1, "Value", headerStyle);

        for (int i = 0; i < labels.length; i++) {
            Row row = sheet.createRow(i + 1);
            createCell(row, 0, labels[i]);
            createCell(row, 1, values[i]);
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void buildActivitiesSheet(Workbook workbook, CellStyle headerStyle,
                                      MonthlyDetailsResponse details) {
        Sheet sheet = workbook.createSheet("Activities");

        String[] headers = {"Date", "Start Time", "End Time", "Activity Type",
                "Activity Subject", "Task Description", "Duration"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            createCell(headerRow, i, headers[i], headerStyle);
        }

        List<ActivityDetailResponse> activities =
                details.getActivities() != null ? details.getActivities() : List.of();

        int rowIdx = 1;
        for (ActivityDetailResponse activity : activities) {
            Row row = sheet.createRow(rowIdx++);
            createCell(row, 0, activity.getDate() != null ? activity.getDate().toString() : "");
            createCell(row, 1, activity.getStartTime() != null
                    ? activity.getStartTime().format(TIME_FORMATTER) : "");
            createCell(row, 2, activity.getEndTime() != null
                    ? activity.getEndTime().format(TIME_FORMATTER) : "");
            createCell(row, 3, activity.getActivityType());
            createCell(row, 4, activity.getActivitySubject());
            createCell(row, 5, activity.getTaskDescription());
            createCell(row, 6, formatMinutes(activity.getDurationMinutes()));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void buildByTypeSheet(Workbook workbook, CellStyle headerStyle,
                                  List<HoursByTypeResponse> byType) {
        Sheet sheet = workbook.createSheet("By Activity Type");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Activity Type", headerStyle);
        createCell(headerRow, 1, "Total Duration", headerStyle);

        int rowIdx = 1;
        for (HoursByTypeResponse entry : byType) {
            Row row = sheet.createRow(rowIdx++);
            createCell(row, 0, entry.getActivityType());
            createCell(row, 1, formatMinutes(entry.getTotalDurationMinutes()));
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void buildBySubjectSheet(Workbook workbook, CellStyle headerStyle,
                                     List<HoursBySubjectResponse> bySubject) {
        Sheet sheet = workbook.createSheet("By Subject");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Activity Subject", headerStyle);
        createCell(headerRow, 1, "Total Duration", headerStyle);

        int rowIdx = 1;
        for (HoursBySubjectResponse entry : bySubject) {
            Row row = sheet.createRow(rowIdx++);
            createCell(row, 0, entry.getActivitySubject());
            createCell(row, 1, formatMinutes(entry.getTotalDurationMinutes()));
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private String formatMinutes(long minutes) {
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        return hours + "h " + remainingMinutes + "m";
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(boldFont);
        return style;
    }

    private void createCell(Row row, int column, String value) {
        createCell(row, column, value, null);
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        if (style != null) {
            cell.setCellStyle(style);
        }
    }
}
