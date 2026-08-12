package com.raya.activitytracking.masterdata.service.impl;

import com.raya.activitytracking.masterdata.service.ActivityTypeService;
import com.raya.activitytracking.masterdata.dto.request.ActivityTypeRequest;
import com.raya.activitytracking.masterdata.dto.response.ActivityTypeResponse;
import com.raya.activitytracking.masterdata.entity.ActivityType;
import com.raya.activitytracking.masterdata.repository.ActivityTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityTypeServiceImpl implements ActivityTypeService {

    private final ActivityTypeRepository activityTypeRepository;

    @Override
    public ActivityTypeResponse create(ActivityTypeRequest request) {
        if (activityTypeRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException(
                    "Activity Type with name '" + request.getName() + "' already exists");
        }

        ActivityType entity = ActivityType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();

        ActivityType saved = activityTypeRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityTypeResponse> getAll() {
        return activityTypeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityTypeResponse getById(Long id) {
        ActivityType entity = findEntityById(id);
        return toResponse(entity);
    }

    @Override
    public ActivityTypeResponse update(Long id, ActivityTypeRequest request) {
        ActivityType entity = findEntityById(id);

        boolean nameChanged = !entity.getName().equalsIgnoreCase(request.getName());
        if (nameChanged && activityTypeRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException(
                    "Activity Type with name '" + request.getName() + "' already exists");
        }

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        ActivityType updated = activityTypeRepository.save(entity);
        return toResponse(updated);
    }

    @Override
    public void deactivate(Long id) {
        ActivityType entity = findEntityById(id);
        entity.setActive(false);
        activityTypeRepository.save(entity);
    }

    private ActivityType findEntityById(Long id) {
        return activityTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Activity Type not found with id: " + id));
    }

    private ActivityTypeResponse toResponse(ActivityType entity) {
        return ActivityTypeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .active(entity.getActive())
                .build();
    }
}