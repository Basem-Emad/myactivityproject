package com.activitytracking.masterdata.service.impl;

import com.activitytracking.masterdata.service.ActivitySubjectService;
import com.activitytracking.masterdata.dto.request.ActivitySubjectRequest;
import com.activitytracking.masterdata.dto.response.ActivitySubjectResponse;
import com.activitytracking.masterdata.entity.ActivitySubject;
import com.activitytracking.masterdata.repository.ActivitySubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivitySubjectServiceImpl implements ActivitySubjectService {

    private final ActivitySubjectRepository activitySubjectRepository;

    @Override
    public ActivitySubjectResponse create(ActivitySubjectRequest request) {
        if (activitySubjectRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException(
                    "Activity Subject with name '" + request.getName() + "' already exists");
        }

        ActivitySubject entity = ActivitySubject.builder()
                .name(request.getName())
                .subjectType(request.getSubjectType())
                .description(request.getDescription())
                .active(true)
                .build();

        ActivitySubject saved = activitySubjectRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivitySubjectResponse> getAll() {
        return activitySubjectRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ActivitySubjectResponse getById(Long id) {
        ActivitySubject entity = findEntityById(id);
        return toResponse(entity);
    }

    @Override
    public ActivitySubjectResponse update(Long id, ActivitySubjectRequest request) {
        ActivitySubject entity = findEntityById(id);

        boolean nameChanged = !entity.getName().equalsIgnoreCase(request.getName());
        if (nameChanged && activitySubjectRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException(
                    "Activity Subject with name '" + request.getName() + "' already exists");
        }

        entity.setName(request.getName());
        entity.setSubjectType(request.getSubjectType());
        entity.setDescription(request.getDescription());

        ActivitySubject updated = activitySubjectRepository.save(entity);
        return toResponse(updated);
    }

    @Override
    public void deactivate(Long id) {
        ActivitySubject entity = findEntityById(id);
        entity.setActive(false);
        activitySubjectRepository.save(entity);
    }

    private ActivitySubject findEntityById(Long id) {
        return activitySubjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Activity Subject not found with id: " + id));
    }

    private ActivitySubjectResponse toResponse(ActivitySubject entity) {
        return ActivitySubjectResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .subjectType(entity.getSubjectType())
                .description(entity.getDescription())
                .active(entity.getActive())
                .build();
    }
}