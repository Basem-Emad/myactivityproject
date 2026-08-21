package com.raya.activitytracking.activity.controller;

import com.raya.activitytracking.activity.dto.request.ActivityEntryRequest;
import com.raya.activitytracking.activity.dto.response.ActivityEntryResponse;
import com.raya.activitytracking.activity.service.ActivityEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Tag(name = "Activity Entries", description = "Daily activity logging and management")
public class ActivityEntryController {

    private final ActivityEntryService activityEntryService;

    // Temporary: hardcoded userId until auth module is integrated
    // Will be replaced with: SecurityContextHolder → JWT → userId
    private static final Long TEMP_USER_ID = 1L;

    @PostMapping
    @Operation(summary = "Create a new activity entry")
    public ResponseEntity<ActivityEntryResponse> create(
            @Valid @RequestBody ActivityEntryRequest request) {
        ActivityEntryResponse response = activityEntryService.create(
                TEMP_USER_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all activities for the current user")
    public ResponseEntity<List<ActivityEntryResponse>> getAll() {
        return ResponseEntity.ok(
                activityEntryService.getByUser(TEMP_USER_ID));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single activity entry by ID")
    public ResponseEntity<ActivityEntryResponse> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(activityEntryService.getById(id, TEMP_USER_ID));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter activities by date range")
    public ResponseEntity<List<ActivityEntryResponse>> filter(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(
                activityEntryService.getByUserAndDateRange(
                        TEMP_USER_ID, startDate, endDate));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing activity entry")
    public ResponseEntity<ActivityEntryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ActivityEntryRequest request) {
        return ResponseEntity.ok(
                activityEntryService.update(id, TEMP_USER_ID, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an activity entry")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        activityEntryService.delete(id, TEMP_USER_ID);
        return ResponseEntity.noContent().build();
    }
}
