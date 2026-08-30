package com.activitytracking.dashboard.service;

import java.io.ByteArrayOutputStream;
import java.time.YearMonth;

public interface ExcelExportService {
    ByteArrayOutputStream exportMonthlyReport(Long userId, YearMonth month);
}