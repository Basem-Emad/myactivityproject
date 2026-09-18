package com.activitytracking.activity.service;

import com.activitytracking.activity.dto.request.ActivityEntryFilter;
import com.activitytracking.activity.dto.request.ActivityEntryRequest;
import com.activitytracking.activity.dto.response.ActivityEntryResponse;

import java.util.List;

public interface ActivityEntryService {

    ActivityEntryResponse create(Long userId, ActivityEntryRequest request);

    // requestingUserId/canViewTeam describe who is asking, so ownership can be enforced
    // for every read/write without the controller having to duplicate that logic.
    ActivityEntryResponse getById(Long id, Long requestingUserId, boolean canViewTeam);

    List<ActivityEntryResponse> getByFilters(ActivityEntryFilter filter, Long requestingUserId, boolean canViewTeam);

    ActivityEntryResponse update(Long id, ActivityEntryRequest request, Long requestingUserId);

    void delete(Long id, Long requestingUserId);
}
