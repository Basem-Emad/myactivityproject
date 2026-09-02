package com.raya.activitytracking.masterdata.controller;

import com.raya.activitytracking.masterdata.dto.request.ActivityTypeRequest;
import com.raya.activitytracking.masterdata.dto.response.ActivityTypeResponse;
import com.raya.activitytracking.masterdata.service.ActivityTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-types")
@RequiredArgsConstructor
@Tag(name = "Activity Types", description = "Manage Activity Type master data")
public class ActivityTypeController {

    private final ActivityTypeService activityTypeService;

    @PostMapping
    public ResponseEntity<ActivityTypeResponse> create(@Valid @RequestBody ActivityTypeRequest request) {
        ActivityTypeResponse response = activityTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ActivityTypeResponse>> getAll() {
        return ResponseEntity.ok(activityTypeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityTypeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(activityTypeService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ActivityTypeRequest request) {
        return ResponseEntity.ok(activityTypeService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ActivityTypeResponse> setActiveStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(activityTypeService.setActiveStatus(id, active));
    }
}