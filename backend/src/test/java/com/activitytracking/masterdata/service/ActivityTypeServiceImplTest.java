package com.activitytracking.masterdata.service;

import com.activitytracking.masterdata.dto.request.ActivityTypeRequest;
import com.activitytracking.masterdata.dto.response.ActivityTypeResponse;
import com.activitytracking.masterdata.entity.ActivityType;
import com.activitytracking.masterdata.repository.ActivityTypeRepository;
import com.activitytracking.masterdata.service.impl.ActivityTypeServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityTypeServiceImplTest {

    @Mock
    private ActivityTypeRepository activityTypeRepository;

    @InjectMocks
    private ActivityTypeServiceImpl activityTypeService;

    private ActivityType existingEntity;
    private ActivityTypeRequest request;

    @BeforeEach
    void setUp() {
        existingEntity = ActivityType.builder()
                .id(1L)
                .name("Project")
                .description("Client project work")
                .active(true)
                .build();

        request = new ActivityTypeRequest();
        request.setName("Project");
        request.setDescription("Client project work");
    }

    @Test
    void create_shouldSaveAndReturnResponse_whenNameDoesNotExist() {
        when(activityTypeRepository.existsByNameIgnoreCase(request.getName())).thenReturn(false);
        when(activityTypeRepository.save(any(ActivityType.class))).thenReturn(existingEntity);

        ActivityTypeResponse response = activityTypeService.create(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Project");
        assertThat(response.getActive()).isTrue();
        verify(activityTypeRepository, times(1)).save(any(ActivityType.class));
    }

    @Test
    void create_shouldThrowException_whenNameAlreadyExists() {
        when(activityTypeRepository.existsByNameIgnoreCase(request.getName())).thenReturn(true);

        assertThatThrownBy(() -> activityTypeService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(activityTypeRepository, never()).save(any(ActivityType.class));
    }

    @Test
    void getById_shouldReturnResponse_whenEntityExists() {
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(existingEntity));

        ActivityTypeResponse response = activityTypeService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Project");
    }

    @Test
    void getById_shouldThrowException_whenEntityDoesNotExist() {
        when(activityTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityTypeService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getAll_shouldReturnListOfResponses() {
        when(activityTypeRepository.findAll()).thenReturn(List.of(existingEntity));

        List<ActivityTypeResponse> responses = activityTypeService.getAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Project");
    }

    @Test
    void update_shouldUpdateAndReturnResponse_whenNameUnchanged() {
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(activityTypeRepository.save(any(ActivityType.class))).thenReturn(existingEntity);

        ActivityTypeResponse response = activityTypeService.update(1L, request);

        assertThat(response.getName()).isEqualTo("Project");
        verify(activityTypeRepository, never()).existsByNameIgnoreCase(anyString());
    }

    @Test
    void update_shouldThrowException_whenNewNameAlreadyExists() {
        request.setName("Product");
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(activityTypeRepository.existsByNameIgnoreCase("Product")).thenReturn(true);

        assertThatThrownBy(() -> activityTypeService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(activityTypeRepository, never()).save(any(ActivityType.class));
    }

    @Test
    void deactivate_shouldSetActiveFalse_whenEntityExists() {
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(activityTypeRepository.save(any(ActivityType.class))).thenReturn(existingEntity);

        activityTypeService.deactivate(1L);

        assertThat(existingEntity.getActive()).isFalse();
        verify(activityTypeRepository, times(1)).save(existingEntity);
    }

    @Test
    void deactivate_shouldThrowException_whenEntityDoesNotExist() {
        when(activityTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityTypeService.deactivate(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(activityTypeRepository, never()).save(any(ActivityType.class));
    }
}