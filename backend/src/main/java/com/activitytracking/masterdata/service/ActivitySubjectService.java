package com.activitytracking.masterdata.service;

import com.activitytracking.masterdata.dto.request.ActivitySubjectRequest;
import com.activitytracking.masterdata.dto.response.ActivitySubjectResponse;

import java.util.List;

public interface ActivitySubjectService {

    ActivitySubjectResponse create(ActivitySubjectRequest request);

    List<ActivitySubjectResponse> getAll();

    ActivitySubjectResponse getById(Long id);

    ActivitySubjectResponse update(Long id, ActivitySubjectRequest request);

    void deactivate(Long id);
}