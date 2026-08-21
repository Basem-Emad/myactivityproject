package com.raya.activitytracking.activity.service.impl;

import com.raya.activitytracking.activity.dto.request.ActivityEntryRequest;
import com.raya.activitytracking.activity.dto.response.ActivityEntryResponse;
import com.raya.activitytracking.activity.entity.ActivityEntry;
import com.raya.activitytracking.activity.repository.ActivityEntryRepository;
import com.raya.activitytracking.activity.service.ActivityEntryService;
import com.raya.activitytracking.masterdata.entity.ActivityType;
import com.raya.activitytracking.masterdata.entity.ActivitySubject;
import com.raya.activitytracking.masterdata.repository.ActivityTypeRepository;
import com.raya.activitytracking.masterdata.repository.ActivitySubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityEntryServiceImpl implements ActivityEntryService {

    private final ActivityEntryRepository activityEntryRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final ActivitySubjectRepository activitySubjectRepository;

    @Override
    public ActivityEntryResponse create(Long userId, ActivityEntryRequest request) {
        // 1. Validate time range
        validateTimeRange(request.getStartTime(), request.getEndTime());

        // 2. Check for overlapping entries
        checkForOverlap(userId, request.getDate(),
                request.getStartTime(), request.getEndTime(), null);

        // 3. Look up related entities
        ActivityType type = findActivityType(request.getActivityTypeId());
        ActivitySubject subject = findActivitySubject(request.getActivitySubjectId());
        validateActiveStatusForCreate(type, subject);

        // 4. Calculate duration
        Integer durationMinutes = calculateDuration(
                request.getStartTime(), request.getEndTime());

        // 5. Build and save
        ActivityEntry entry = ActivityEntry.builder()
                .userId(userId)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes(durationMinutes)
                .activityType(type)
                .activitySubject(subject)
                .taskDescription(request.getTaskDescription())
                .build();

        ActivityEntry saved = activityEntryRepository.save(entry);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityEntryResponse getById(Long id, Long userId) {
        ActivityEntry entry = activityEntryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Activity entry not found with id: " + id));
        return toResponse(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityEntryResponse> getByUser(Long userId) {
        return activityEntryRepository
                .findByUserIdOrderByDateDescStartTimeDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityEntryResponse> getByUserAndDateRange(
            Long userId, LocalDate startDate, LocalDate endDate) {
        return activityEntryRepository
                .findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
                        userId, startDate, endDate)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ActivityEntryResponse update(Long id, Long userId,
            ActivityEntryRequest request) {
        ActivityEntry entry = findEntityById(id);

        // Security: ensure the entry belongs to the requesting user
        if (!entry.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You can only edit your own activity entries");
        }

        validateTimeRange(request.getStartTime(), request.getEndTime());
        checkForOverlap(userId, request.getDate(),
                request.getStartTime(), request.getEndTime(), id);

        ActivityType type = findActivityType(request.getActivityTypeId());
        ActivitySubject subject = findActivitySubject(request.getActivitySubjectId());
        validateActiveStatusForUpdate(entry, type, subject);
        Integer durationMinutes = calculateDuration(
                request.getStartTime(), request.getEndTime());

        entry.setDate(request.getDate());
        entry.setStartTime(request.getStartTime());
        entry.setEndTime(request.getEndTime());
        entry.setDurationMinutes(durationMinutes);
        entry.setActivityType(type);
        entry.setActivitySubject(subject);
        entry.setTaskDescription(request.getTaskDescription());

        ActivityEntry updated = activityEntryRepository.save(entry);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id, Long userId) {
        ActivityEntry entry = findEntityById(id);
        if (!entry.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You can only delete your own activity entries");
        }
        activityEntryRepository.delete(entry);
    }

    // ─── BUSINESS LOGIC ─────────────────────────────────────────

    /**
     * Calculates duration between two times in minutes.
     * Example: 09:00 to 12:30 → 210 minutes
     */
    Integer calculateDuration(LocalTime start, LocalTime end) {
        return (int) Duration.between(start, end).toMinutes();
    }

    /**
     * Validates that end time is strictly after start time.
     */
    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException(
                    "End time must be after start time");
        }
    }

    /**
     * Checks for overlapping entries. Throws if any overlap is found.
     * 
     * @param excludeId ID to exclude (for updates — don't conflict with yourself)
     */
    private void checkForOverlap(Long userId, LocalDate date,
            LocalTime startTime, LocalTime endTime,
            Long excludeId) {
        List<ActivityEntry> overlapping = activityEntryRepository
                .findOverlapping(userId, date, startTime, endTime, excludeId);
        if (!overlapping.isEmpty()) {
            ActivityEntry conflict = overlapping.get(0);
            throw new IllegalArgumentException(String.format(
                    "This activity overlaps with an existing entry (%s - %s)",
                    conflict.getStartTime(), conflict.getEndTime()));
        }
    }

    /**
     * Validates that both activity type and subject are active for new entries.
     */
    private void validateActiveStatusForCreate(ActivityType type, ActivitySubject subject) {
        if (!Boolean.TRUE.equals(type.getActive())) {
            throw new IllegalArgumentException(
                    "Cannot create activity with inactive Activity Type: " + type.getName());
        }
        if (!Boolean.TRUE.equals(subject.getActive())) {
            throw new IllegalArgumentException(
                    "Cannot create activity with inactive Activity Subject: " + subject.getName());
        }
    }

    /**
     * Validates that any newly selected activity type or subject is active on update.
     * Preserves existing references even if they were subsequently deactivated.
     */
    private void validateActiveStatusForUpdate(ActivityEntry existingEntry,
                                               ActivityType newType,
                                               ActivitySubject newSubject) {
        boolean typeChanged = !existingEntry.getActivityType().getId().equals(newType.getId());
        if (typeChanged && !Boolean.TRUE.equals(newType.getActive())) {
            throw new IllegalArgumentException(
                    "Cannot change to inactive Activity Type: " + newType.getName());
        }

        boolean subjectChanged = !existingEntry.getActivitySubject().getId().equals(newSubject.getId());
        if (subjectChanged && !Boolean.TRUE.equals(newSubject.getActive())) {
            throw new IllegalArgumentException(
                    "Cannot change to inactive Activity Subject: " + newSubject.getName());
        }
    }

    // ─── HELPERS ─────────────────────────────────────────────────

    private ActivityEntry findEntityById(Long id) {
        return activityEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Activity entry not found with id: " + id));
    }

    private ActivityType findActivityType(Long id) {
        return activityTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Activity type not found with id: " + id));
    }

    private ActivitySubject findActivitySubject(Long id) {
        return activitySubjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Activity subject not found with id: " + id));
    }

    private ActivityEntryResponse toResponse(ActivityEntry entry) {
        int totalMinutes = entry.getDurationMinutes();
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        String formatted = hours + "h " + minutes + "m";

        return ActivityEntryResponse.builder()
                .id(entry.getId())
                .userId(entry.getUserId())
                .date(entry.getDate())
                .startTime(entry.getStartTime())
                .endTime(entry.getEndTime())
                .durationMinutes(entry.getDurationMinutes())
                .durationFormatted(formatted)
                .activityTypeId(entry.getActivityType().getId())
                .activityTypeName(entry.getActivityType().getName())
                .activitySubjectId(entry.getActivitySubject().getId())
                .activitySubjectName(entry.getActivitySubject().getName())
                .taskDescription(entry.getTaskDescription())
                .build();
    }
}
