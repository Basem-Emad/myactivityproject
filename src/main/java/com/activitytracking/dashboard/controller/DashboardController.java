package com.activitytracking.dashboard.controller;

import com.activitytracking.dashboard.dto.response.HoursBySubjectResponse;
import com.activitytracking.dashboard.dto.response.HoursByTypeResponse;
import com.activitytracking.dashboard.dto.response.MonthlySummaryResponse;
import com.activitytracking.dashboard.service.DashboardService;
import com.activitytracking.dashboard.service.ExcelExportService;
import com.activitytracking.user.constants.PermissionNames;
import com.activitytracking.user.entity.User;
import com.activitytracking.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final ExcelExportService excelExportService;
    private final UserRepository userRepository;

    public DashboardController(
            DashboardService dashboardService,
            ExcelExportService excelExportService,
            UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.excelExportService = excelExportService;
        this.userRepository = userRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryResponse> getSummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(required = false) Long userId,
            Authentication authentication) {

        Long targetUserId = resolveTargetUserId(userId, authentication);
        return ResponseEntity.ok(dashboardService.getMonthlySummary(targetUserId, month));
    }

    @GetMapping("/by-type")
    public ResponseEntity<List<HoursByTypeResponse>> getHoursByType(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(required = false) Long userId,
            Authentication authentication) {

        Long targetUserId = resolveTargetUserId(userId, authentication);
        return ResponseEntity.ok(dashboardService.getHoursByType(targetUserId, month));
    }

    @GetMapping("/by-subject")
    public ResponseEntity<List<HoursBySubjectResponse>> getHoursBySubject(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(required = false) Long userId,
            Authentication authentication) {

        Long targetUserId = resolveTargetUserId(userId, authentication);
        return ResponseEntity.ok(dashboardService.getHoursBySubject(targetUserId, month));
    }

    @GetMapping("/reports/export/monthly")
    public ResponseEntity<byte[]> exportMonthlyReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(required = false) Long userId,
            Authentication authentication) {

        Long targetUserId = resolveTargetUserId(userId, authentication);
        ByteArrayOutputStream excelFile = excelExportService.exportMonthlyReport(targetUserId, month);

        String filename = "monthly_report_" + month + ".xlsx";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelFile.toByteArray());
    }

    private Long resolveUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
        return user.getId();
    }

    // No userId param -> the caller's own dashboard. An explicit userId is only honored
    // for callers with ACTIVITY_VIEW_TEAM (Manager/Admin); anyone else asking for someone
    // else's dashboard is rejected rather than silently redirected to their own.
    private Long resolveTargetUserId(Long requestedUserId, Authentication authentication) {
        Long ownUserId = resolveUserId(authentication);

        if (requestedUserId == null || requestedUserId.equals(ownUserId)) {
            return ownUserId;
        }

        boolean canViewTeam = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(PermissionNames.ACTIVITY_VIEW_TEAM::equals);

        if (!canViewTeam) {
            throw new AccessDeniedException("You can only view your own dashboard");
        }

        return requestedUserId;
    }
}
