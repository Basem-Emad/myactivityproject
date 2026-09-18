package com.activitytracking.masterdata.controller;

import com.activitytracking.masterdata.dto.request.ActivityTypeRequest;
import com.activitytracking.masterdata.dto.response.ActivityTypeResponse;
import com.activitytracking.masterdata.service.ActivityTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.activitytracking.user.constants.PermissionNames;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activity-types")
@RequiredArgsConstructor
@Tag(name = "Activity Types", description = "Manage Activity Type master data")
public class ActivityTypeController {

    private final ActivityTypeService activityTypeService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.MASTERDATA_MANAGE + "')")
    public ResponseEntity<ActivityTypeResponse> create(@Valid @RequestBody ActivityTypeRequest request) {
        ActivityTypeResponse response = activityTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Intentionally open to any authenticated user (not just Admin): every employee
    // needs to see the active Activity Types/Subjects to log their own activities.
    @GetMapping
    public ResponseEntity<List<ActivityTypeResponse>> getAll() {
        return ResponseEntity.ok(activityTypeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityTypeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(activityTypeService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.MASTERDATA_MANAGE + "')")
    public ResponseEntity<ActivityTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ActivityTypeRequest request) {
        return ResponseEntity.ok(activityTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.MASTERDATA_MANAGE + "')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        activityTypeService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}