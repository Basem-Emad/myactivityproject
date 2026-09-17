package com.activitytracking.activity.controller;

import com.activitytracking.activity.dto.request.ActivityEntryFilter;
import com.activitytracking.activity.dto.request.ActivityEntryRequest;
import com.activitytracking.activity.dto.response.ActivityEntryResponse;
import com.activitytracking.activity.service.ActivityEntryService;
import com.activitytracking.user.constants.PermissionNames;
import com.activitytracking.user.entity.User;
import com.activitytracking.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
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
    public ResponseEntity<ActivityEntryResponse> getById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(
                activityEntryService.getById(id, resolveUserId(authentication), canViewTeam(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<ActivityEntryResponse>> getByFilters(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long activityTypeId,
            @RequestParam(required = false) Long activitySubjectId,
            Authentication authentication) {

        ActivityEntryFilter filter = new ActivityEntryFilter(
                userId, date, fromDate, toDate, activityTypeId, activitySubjectId);

        List<ActivityEntryResponse> results = activityEntryService.getByFilters(
                filter, resolveUserId(authentication), canViewTeam(authentication));

        return ResponseEntity.ok(results);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityEntryResponse> update(
            @PathVariable Long id, @Valid @RequestBody ActivityEntryRequest request, Authentication authentication) {
        return ResponseEntity.ok(
                activityEntryService.update(id, request, resolveUserId(authentication)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        activityEntryService.delete(id, resolveUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
        return user.getId();
    }

    private boolean canViewTeam(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(PermissionNames.ACTIVITY_VIEW_TEAM::equals);
    }
}
