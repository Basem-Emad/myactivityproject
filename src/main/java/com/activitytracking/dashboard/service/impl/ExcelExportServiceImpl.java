package com.activitytracking.dashboard.service.impl;

import com.activitytracking.activity.entity.ActivityEntry;
import com.activitytracking.activity.repository.ActivityEntryRepository;
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

    public ExcelExportServiceImpl(ActivityEntryRepository activityEntryRepository) {
        this.activityEntryRepository = activityEntryRepository;
    }

    @Override
    public ByteArrayOutputStream exportMonthlyReport(Long userId, YearMonth month) {

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        List<ActivityEntry> entries =
                activityEntryRepository.findByUserIdAndActivityDateBetween(userId, startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Monthly Report");

            Row header = sheet.createRow(0);
            String[] columns = {"Date", "Start Time", "End Time", "Duration (min)", "Activity Type", "Activity Subject", "Task Description"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

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

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream;

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate Excel report", e);
        }
    }
}