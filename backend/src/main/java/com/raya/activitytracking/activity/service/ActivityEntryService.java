package com.raya.activitytracking.activity.service;

import com.raya.activitytracking.activity.dto.request.ActivityEntryRequest;
import com.raya.activitytracking.activity.dto.response.ActivityEntryResponse;
import java.time.LocalDate;
import java.util.List;

public interface ActivityEntryService {

    ActivityEntryResponse create(Long userId, ActivityEntryRequest request);

    ActivityEntryResponse getById(Long id, Long userId);

    List<ActivityEntryResponse> getByUser(Long userId);

    List<ActivityEntryResponse> getByUserAndDateRange(
            Long userId, LocalDate startDate, LocalDate endDate);

    ActivityEntryResponse update(Long id, Long userId, ActivityEntryRequest request);

    void delete(Long id, Long userId);
}
