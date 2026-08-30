package com.activitytracking.activity.service;

import com.activitytracking.activity.dto.request.ActivityEntryRequest;
import com.activitytracking.activity.dto.response.ActivityEntryResponse;

import java.time.LocalDate;
import java.util.List;

public interface ActivityEntryService {
    ActivityEntryResponse create(Long userId, ActivityEntryRequest request);
    ActivityEntryResponse getById(Long id);
    List<ActivityEntryResponse> getByFilters(Long userId, LocalDate date, Long activityTypeId, Long activitySubjectId);
    ActivityEntryResponse update(Long id, ActivityEntryRequest request);
    void delete(Long id);
}