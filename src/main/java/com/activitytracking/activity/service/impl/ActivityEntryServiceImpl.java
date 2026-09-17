package com.activitytracking.activity.service.impl;

import com.activitytracking.activity.dto.request.ActivityEntryFilter;
import com.activitytracking.activity.dto.request.ActivityEntryRequest;
import com.activitytracking.activity.dto.response.ActivityEntryResponse;
import com.activitytracking.activity.entity.ActivityEntry;
import com.activitytracking.activity.repository.ActivityEntryRepository;
import com.activitytracking.activity.service.ActivityEntryService;
import com.activitytracking.activity.specification.ActivityEntrySpecification;
import com.activitytracking.masterdata.entity.ActivitySubject;
import com.activitytracking.masterdata.entity.ActivityType;
import com.activitytracking.masterdata.repository.ActivitySubjectRepository;
import com.activitytracking.masterdata.repository.ActivityTypeRepository;
import com.activitytracking.user.entity.User;
import com.activitytracking.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ActivityEntryServiceImpl implements ActivityEntryService {

    private final ActivityEntryRepository activityEntryRepository;
    private final UserRepository userRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final ActivitySubjectRepository activitySubjectRepository;

    public ActivityEntryServiceImpl(
            ActivityEntryRepository activityEntryRepository,
            UserRepository userRepository,
            ActivityTypeRepository activityTypeRepository,
            ActivitySubjectRepository activitySubjectRepository) {
        this.activityEntryRepository = activityEntryRepository;
        this.userRepository = userRepository;
        this.activityTypeRepository = activityTypeRepository;
        this.activitySubjectRepository = activitySubjectRepository;
    }

    @Override
    public ActivityEntryResponse create(Long userId, ActivityEntryRequest request) {

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be later than start time");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        ActivityType activityType = activityTypeRepository.findById(request.getActivityTypeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Activity type not found with id: " + request.getActivityTypeId()));

        ActivitySubject activitySubject = activitySubjectRepository.findById(request.getActivitySubjectId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Activity subject not found with id: " + request.getActivitySubjectId()));

        checkForOverlap(userId, request.getActivityDate(), request.getStartTime(), request.getEndTime(), null);

        int durationMinutes = (int) Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();

        ActivityEntry entry = new ActivityEntry();
        entry.setUser(user);
        entry.setActivityType(activityType);
        entry.setActivitySubject(activitySubject);
        entry.setActivityDate(request.getActivityDate());
        entry.setStartTime(request.getStartTime());
        entry.setEndTime(request.getEndTime());
        entry.setDurationMinutes(durationMinutes);
        entry.setTaskDescription(request.getTaskDescription());

        ActivityEntry saved = activityEntryRepository.save(entry);

        return toResponseDto(saved);
    }

    @Override
    public ActivityEntryResponse getById(Long id, Long requestingUserId, boolean canViewTeam) {
        ActivityEntry entry = activityEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Activity entry not found with id: " + id));

        assertCanView(entry, requestingUserId, canViewTeam);

        return toResponseDto(entry);
    }

    @Override
    public List<ActivityEntryResponse> getByFilters(
            ActivityEntryFilter filter, Long requestingUserId, boolean canViewTeam) {

        if (filter.fromDate() != null && filter.toDate() != null && filter.fromDate().isAfter(filter.toDate())) {
            throw new IllegalArgumentException("fromDate must not be after toDate");
        }

        ActivityEntryFilter effectiveFilter = scopeFilterToCaller(filter, requestingUserId, canViewTeam);

        Specification<ActivityEntry> spec = ActivityEntrySpecification.withFilters(effectiveFilter);

        return activityEntryRepository.findAll(spec).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public ActivityEntryResponse update(Long id, ActivityEntryRequest request, Long requestingUserId) {

        ActivityEntry entry = activityEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Activity entry not found with id: " + id));

        // Editing is always owner-only, regardless of ACTIVITY_VIEW_TEAM: a manager can
        // see the team's entries, but only the owner can change their own logged work.
        assertIsOwner(entry, requestingUserId);

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be later than start time");
        }

        ActivityType activityType = activityTypeRepository.findById(request.getActivityTypeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Activity type not found with id: " + request.getActivityTypeId()));

        ActivitySubject activitySubject = activitySubjectRepository.findById(request.getActivitySubjectId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Activity subject not found with id: " + request.getActivitySubjectId()));

        checkForOverlap(entry.getUser().getId(), request.getActivityDate(),
                request.getStartTime(), request.getEndTime(), id);

        int durationMinutes = (int) Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();

        entry.setActivityType(activityType);
        entry.setActivitySubject(activitySubject);
        entry.setActivityDate(request.getActivityDate());
        entry.setStartTime(request.getStartTime());
        entry.setEndTime(request.getEndTime());
        entry.setDurationMinutes(durationMinutes);
        entry.setTaskDescription(request.getTaskDescription());

        ActivityEntry saved = activityEntryRepository.save(entry);

        return toResponseDto(saved);
    }

    @Override
    public void delete(Long id, Long requestingUserId) {
        ActivityEntry entry = activityEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Activity entry not found with id: " + id));

        assertIsOwner(entry, requestingUserId);

        activityEntryRepository.deleteById(id);
    }

    // A caller with ACTIVITY_VIEW_TEAM can read anyone's entries (their explicit userId
    // filter, or everyone's if they didn't specify one). Anyone else is silently scoped
    // to their own data, and gets rejected if they explicitly asked for someone else's.
    private ActivityEntryFilter scopeFilterToCaller(
            ActivityEntryFilter filter, Long requestingUserId, boolean canViewTeam) {

        if (canViewTeam) {
            return filter;
        }

        if (filter.userId() != null && !filter.userId().equals(requestingUserId)) {
            throw new AccessDeniedException("You can only view your own activity entries");
        }

        return new ActivityEntryFilter(
                requestingUserId, filter.date(), filter.fromDate(), filter.toDate(),
                filter.activityTypeId(), filter.activitySubjectId());
    }

    private void assertCanView(ActivityEntry entry, Long requestingUserId, boolean canViewTeam) {
        boolean isOwner = entry.getUser().getId().equals(requestingUserId);

        if (!isOwner && !canViewTeam) {
            throw new AccessDeniedException("You can only view your own activity entries");
        }
    }

    private void assertIsOwner(ActivityEntry entry, Long requestingUserId) {
        if (!entry.getUser().getId().equals(requestingUserId)) {
            throw new AccessDeniedException("You can only modify your own activity entries");
        }
    }

    private void checkForOverlap(Long userId, LocalDate date, LocalTime newStart, LocalTime newEnd, Long excludeEntryId) {
        List<ActivityEntry> existingEntries = activityEntryRepository.findByUserIdAndActivityDate(userId, date);

        boolean hasOverlap = existingEntries.stream()
                .filter(existing -> !existing.getId().equals(excludeEntryId))
                .anyMatch(existing -> newStart.isBefore(existing.getEndTime())
                        && existing.getStartTime().isBefore(newEnd));

        if (hasOverlap) {
            throw new IllegalArgumentException(
                    "This time slot overlaps with an existing activity entry on " + date);
        }
    }

    private ActivityEntryResponse toResponseDto(ActivityEntry entry) {
        return new ActivityEntryResponse(
                entry.getId(),
                entry.getActivityDate(),
                entry.getStartTime(),
                entry.getEndTime(),
                entry.getDurationMinutes(),
                entry.getTaskDescription(),
                entry.getUser().getId(),
                entry.getUser().getName(),
                entry.getActivityType().getId(),
                entry.getActivityType().getName(),
                entry.getActivitySubject().getId(),
                entry.getActivitySubject().getName()
        );
    }
}
