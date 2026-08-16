package com.raya.activitytracking.masterdata.service;

import com.raya.activitytracking.masterdata.dto.request.ActivitySubjectRequest;
import com.raya.activitytracking.masterdata.dto.response.ActivitySubjectResponse;
import com.raya.activitytracking.masterdata.entity.ActivitySubject;
import com.raya.activitytracking.masterdata.entity.SubjectType;
import com.raya.activitytracking.masterdata.repository.ActivitySubjectRepository;
import com.raya.activitytracking.masterdata.service.impl.ActivitySubjectServiceImpl;
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
class ActivitySubjectServiceImplTest {

    @Mock
    private ActivitySubjectRepository activitySubjectRepository;

    @InjectMocks
    private ActivitySubjectServiceImpl activitySubjectService;

    private ActivitySubject existingEntity;
    private ActivitySubjectRequest request;

    @BeforeEach
    void setUp() {
        existingEntity = ActivitySubject.builder()
                .id(1L)
                .name("BM Microfocus")
                .subjectType(SubjectType.PROJECT)
                .description("Core project subject")
                .active(true)
                .build();

        request = new ActivitySubjectRequest();
        request.setName("BM Microfocus");
        request.setSubjectType(SubjectType.PROJECT);
        request.setDescription("Core project subject");
    }

    @Test
    void create_shouldSaveAndReturnResponse_whenNameDoesNotExist() {
        when(activitySubjectRepository.existsByNameIgnoreCase(request.getName())).thenReturn(false);
        when(activitySubjectRepository.save(any(ActivitySubject.class))).thenReturn(existingEntity);

        ActivitySubjectResponse response = activitySubjectService.create(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("BM Microfocus");
        assertThat(response.getSubjectType()).isEqualTo(SubjectType.PROJECT);
        assertThat(response.getActive()).isTrue();
        verify(activitySubjectRepository, times(1)).save(any(ActivitySubject.class));
    }

    @Test
    void create_shouldThrowException_whenNameAlreadyExists() {
        when(activitySubjectRepository.existsByNameIgnoreCase(request.getName())).thenReturn(true);

        assertThatThrownBy(() -> activitySubjectService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(activitySubjectRepository, never()).save(any(ActivitySubject.class));
    }

    @Test
    void getById_shouldReturnResponse_whenEntityExists() {
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(existingEntity));

        ActivitySubjectResponse response = activitySubjectService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("BM Microfocus");
    }

    @Test
    void getById_shouldThrowException_whenEntityDoesNotExist() {
        when(activitySubjectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activitySubjectService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getAll_shouldReturnListOfResponses() {
        when(activitySubjectRepository.findAll()).thenReturn(List.of(existingEntity));

        List<ActivitySubjectResponse> responses = activitySubjectService.getAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("BM Microfocus");
    }

    @Test
    void update_shouldUpdateAndReturnResponse_whenNameUnchanged() {
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(activitySubjectRepository.save(any(ActivitySubject.class))).thenReturn(existingEntity);

        ActivitySubjectResponse response = activitySubjectService.update(1L, request);

        assertThat(response.getName()).isEqualTo("BM Microfocus");
        verify(activitySubjectRepository, never()).existsByNameIgnoreCase(anyString());
    }

    @Test
    void update_shouldThrowException_whenNewNameAlreadyExists() {
        request.setName("License Module");
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(activitySubjectRepository.existsByNameIgnoreCase("License Module")).thenReturn(true);

        assertThatThrownBy(() -> activitySubjectService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(activitySubjectRepository, never()).save(any(ActivitySubject.class));
    }

    @Test
    void deactivate_shouldSetActiveFalse_whenEntityExists() {
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(activitySubjectRepository.save(any(ActivitySubject.class))).thenReturn(existingEntity);

        activitySubjectService.deactivate(1L);

        assertThat(existingEntity.getActive()).isFalse();
        verify(activitySubjectRepository, times(1)).save(existingEntity);
    }

    @Test
    void deactivate_shouldThrowException_whenEntityDoesNotExist() {
        when(activitySubjectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activitySubjectService.deactivate(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(activitySubjectRepository, never()).save(any(ActivitySubject.class));
    }
}