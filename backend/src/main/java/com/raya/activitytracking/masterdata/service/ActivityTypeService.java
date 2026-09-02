package com.raya.activitytracking.masterdata.service;

import com.raya.activitytracking.masterdata.dto.request.ActivityTypeRequest;
import com.raya.activitytracking.masterdata.dto.response.ActivityTypeResponse;

import java.util.List;

public interface ActivityTypeService {

    ActivityTypeResponse create(ActivityTypeRequest request);

    List<ActivityTypeResponse> getAll();

    ActivityTypeResponse getById(Long id);

    ActivityTypeResponse update(Long id, ActivityTypeRequest request);

    ActivityTypeResponse setActiveStatus(Long id, boolean active);
}