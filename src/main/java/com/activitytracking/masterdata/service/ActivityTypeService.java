package com.activitytracking.masterdata.service;

import com.activitytracking.masterdata.dto.request.ActivityTypeRequest;
import com.activitytracking.masterdata.dto.response.ActivityTypeResponse;

import java.util.List;

public interface ActivityTypeService {

    ActivityTypeResponse create(ActivityTypeRequest request);

    List<ActivityTypeResponse> getAll();

    ActivityTypeResponse getById(Long id);

    ActivityTypeResponse update(Long id, ActivityTypeRequest request);

    void deactivate(Long id);
}