package com.raya.activitytracking.masterdata.controller;

import com.raya.activitytracking.masterdata.dto.request.ActivitySubjectRequest;
import com.raya.activitytracking.masterdata.dto.response.ActivitySubjectResponse;
import com.raya.activitytracking.masterdata.service.ActivitySubjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-subjects")
@RequiredArgsConstructor
@Tag(name = "Activity Subjects", description = "Manage Activity Subject master data")
public class ActivitySubjectController {

    private final ActivitySubjectService activitySubjectService;

    @PostMapping
    public ResponseEntity<ActivitySubjectResponse> create(@Valid @RequestBody ActivitySubjectRequest request) {
        ActivitySubjectResponse response = activitySubjectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ActivitySubjectResponse>> getAll() {
        return ResponseEntity.ok(activitySubjectService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivitySubjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(activitySubjectService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivitySubjectResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ActivitySubjectRequest request) {
        return ResponseEntity.ok(activitySubjectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        activitySubjectService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}