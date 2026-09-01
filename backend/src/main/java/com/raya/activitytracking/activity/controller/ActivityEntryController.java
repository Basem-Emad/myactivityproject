package com.raya.activitytracking.activity.controller;

import com.raya.activitytracking.activity.dto.request.ActivityEntryRequest;
import com.raya.activitytracking.activity.dto.response.ActivityEntryResponse;
import com.raya.activitytracking.activity.service.ActivityEntryService;
import com.raya.activitytracking.usermanagement.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Tag(name = "Activity Entries", description = "Daily activity logging and management")
public class ActivityEntryController {

    private final ActivityEntryService activityEntryService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }
        throw new IllegalStateException("Unexpected principal type: " + (principal != null ? principal.getClass().getName() : "null"));
    }

    @PostMapping
    @Operation(summary = "Create a new activity entry")
    public ResponseEntity<ActivityEntryResponse> create(
            @Valid @RequestBody ActivityEntryRequest request) {
        ActivityEntryResponse response = activityEntryService.create(
                getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all activities for the current user")
    public ResponseEntity<List<ActivityEntryResponse>> getAll() {
        return ResponseEntity.ok(
                activityEntryService.getByUser(getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single activity entry by ID")
    public ResponseEntity<ActivityEntryResponse> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(activityEntryService.getById(id, getCurrentUserId()));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter activities by date range")
    public ResponseEntity<List<ActivityEntryResponse>> filter(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(
                activityEntryService.getByUserAndDateRange(
                        getCurrentUserId(), startDate, endDate));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing activity entry")
    public ResponseEntity<ActivityEntryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ActivityEntryRequest request) {
        return ResponseEntity.ok(
                activityEntryService.update(id, getCurrentUserId(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an activity entry")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        activityEntryService.delete(id, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
