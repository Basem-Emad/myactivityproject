package com.activitytracking.activity.controller;

import com.activitytracking.activity.dto.request.ActivityEntryRequest;
import com.activitytracking.activity.dto.response.ActivityEntryResponse;
import com.activitytracking.activity.service.ActivityEntryService;
import com.activitytracking.user.entity.User;
import com.activitytracking.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityEntryController {

    private final ActivityEntryService activityEntryService;
    private final UserRepository userRepository;

    public ActivityEntryController(ActivityEntryService activityEntryService, UserRepository userRepository) {
        this.activityEntryService = activityEntryService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ActivityEntryResponse> create(
            @Valid @RequestBody ActivityEntryRequest request,
            Authentication authentication) {

        Long currentUserId = resolveUserId(authentication);
        ActivityEntryResponse response = activityEntryService.create(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityEntryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(activityEntryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ActivityEntryResponse>> getByFilters(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long activityTypeId,
            @RequestParam(required = false) Long activitySubjectId) {

        List<ActivityEntryResponse> results =
                activityEntryService.getByFilters(userId, date, activityTypeId, activitySubjectId);
        return ResponseEntity.ok(results);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityEntryResponse> update(
            @PathVariable Long id, @Valid @RequestBody ActivityEntryRequest request) {
        return ResponseEntity.ok(activityEntryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        activityEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
        return user.getId();
    }
}