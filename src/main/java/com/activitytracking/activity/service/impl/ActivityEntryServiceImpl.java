package com.activitytracking.activity.service.impl;

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
    public ActivityEntryResponse getById(Long id) {
        ActivityEntry entry = activityEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Activity entry not found with id: " + id));
        return toResponseDto(entry);
    }

    @Override
    public List<ActivityEntryResponse> getByFilters(
            Long userId, LocalDate date, Long activityTypeId, Long activitySubjectId) {

        Specification<ActivityEntry> spec = ActivityEntrySpecification.withFilters(
                userId, date, activityTypeId, activitySubjectId);

        return activityEntryRepository.findAll(spec).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public ActivityEntryResponse update(Long id, ActivityEntryRequest request) {

        ActivityEntry entry = activityEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Activity entry not found with id: " + id));

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
    public void delete(Long id) {
        if (!activityEntryRepository.existsById(id)) {
            throw new EntityNotFoundException("Activity entry not found with id: " + id);
        }
        activityEntryRepository.deleteById(id);
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