package com.activitytracking.activity.service;

import com.activitytracking.activity.dto.request.ActivityEntryFilter;
import com.activitytracking.activity.dto.request.ActivityEntryRequest;
import com.activitytracking.activity.dto.response.ActivityEntryResponse;
import com.activitytracking.activity.entity.ActivityEntry;
import com.activitytracking.activity.repository.ActivityEntryRepository;
import com.activitytracking.activity.service.impl.ActivityEntryServiceImpl;
import com.activitytracking.masterdata.entity.ActivitySubject;
import com.activitytracking.masterdata.entity.ActivityType;
import com.activitytracking.masterdata.entity.SubjectType;
import com.activitytracking.masterdata.repository.ActivitySubjectRepository;
import com.activitytracking.masterdata.repository.ActivityTypeRepository;
import com.activitytracking.user.entity.Role;
import com.activitytracking.user.entity.User;
import com.activitytracking.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityEntryServiceImplTest {

    @Mock
    private ActivityEntryRepository activityEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityTypeRepository activityTypeRepository;

    @Mock
    private ActivitySubjectRepository activitySubjectRepository;

    @InjectMocks
    private ActivityEntryServiceImpl activityEntryService;

    private User user;
    private ActivityType activityType;
    private ActivitySubject activitySubject;
    private ActivityEntryRequest request;
    private ActivityEntry existingEntry;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Ahmed Yehia");
        user.setEmail("ahmed@myproject.com");
        user.setRole(Role.builder().id(1L).name("EMPLOYEE").build());
        user.setActive(true);

        activityType = ActivityType.builder()
                .id(1L)
                .name("Project")
                .active(true)
                .build();

        activitySubject = ActivitySubject.builder()
                .id(1L)
                .name("BM Microfocus")
                .subjectType(SubjectType.PROJECT)
                .active(true)
                .build();

        request = new ActivityEntryRequest();
        request.setActivityDate(LocalDate.of(2026, 8, 27));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(11, 0));
        request.setActivityTypeId(1L);
        request.setActivitySubjectId(1L);
        request.setTaskDescription("Working on backend API");

        existingEntry = new ActivityEntry();
        existingEntry.setId(1L);
        existingEntry.setUser(user);
        existingEntry.setActivityType(activityType);
        existingEntry.setActivitySubject(activitySubject);
        existingEntry.setActivityDate(LocalDate.of(2026, 8, 27));
        existingEntry.setStartTime(LocalTime.of(9, 0));
        existingEntry.setEndTime(LocalTime.of(11, 0));
        existingEntry.setDurationMinutes(120);
        existingEntry.setTaskDescription("Working on backend API");

        otherUserId = 2L;
    }

    // ---------- Duration calculation ----------

    @Test
    void create_shouldCalculateDurationCorrectly_whenValidRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(activityType));
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(activitySubject));
        when(activityEntryRepository.findByUserIdAndActivityDate(1L, request.getActivityDate()))
                .thenReturn(List.of());
        when(activityEntryRepository.save(any(ActivityEntry.class))).thenReturn(existingEntry);

        ActivityEntryResponse response = activityEntryService.create(1L, request);

        assertThat(response.getDurationMinutes()).isEqualTo(120);
    }

    // ---------- Validation: End > Start ----------

    @Test
    void create_shouldThrowException_whenEndTimeBeforeStartTime() {
        request.setStartTime(LocalTime.of(11, 0));
        request.setEndTime(LocalTime.of(9, 0));

        assertThatThrownBy(() -> activityEntryService.create(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End time must be later than start time");

        verify(activityEntryRepository, never()).save(any(ActivityEntry.class));
    }

    @Test
    void create_shouldThrowException_whenEndTimeEqualsStartTime() {
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(9, 0));

        assertThatThrownBy(() -> activityEntryService.create(1L, request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(activityEntryRepository, never()).save(any(ActivityEntry.class));
    }

    // ---------- Validation: referenced entities must exist ----------

    @Test
    void create_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityEntryService.create(99L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void create_shouldThrowException_whenActivityTypeDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityEntryService.create(1L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Activity type not found");
    }

    @Test
    void create_shouldThrowException_whenActivitySubjectDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(activityType));
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityEntryService.create(1L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Activity subject not found");
    }

    // ---------- Overlap detection: edge cases ----------

    @Test
    void create_shouldThrowException_whenExactOverlap() {
        // existing: 09:00-11:00, new: 09:00-11:00 (identical)
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(activityType));
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(activitySubject));
        when(activityEntryRepository.findByUserIdAndActivityDate(1L, request.getActivityDate()))
                .thenReturn(List.of(existingEntry));

        assertThatThrownBy(() -> activityEntryService.create(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("overlaps");

        verify(activityEntryRepository, never()).save(any(ActivityEntry.class));
    }

    @Test
    void create_shouldThrowException_whenPartialOverlapAtStart() {
        // existing: 09:00-11:00, new: 08:00-10:00 (overlaps the beginning)
        request.setStartTime(LocalTime.of(8, 0));
        request.setEndTime(LocalTime.of(10, 0));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(activityType));
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(activitySubject));
        when(activityEntryRepository.findByUserIdAndActivityDate(1L, request.getActivityDate()))
                .thenReturn(List.of(existingEntry));

        assertThatThrownBy(() -> activityEntryService.create(1L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_shouldThrowException_whenPartialOverlapAtEnd() {
        // existing: 09:00-11:00, new: 10:00-12:00 (overlaps the end)
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(12, 0));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(activityType));
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(activitySubject));
        when(activityEntryRepository.findByUserIdAndActivityDate(1L, request.getActivityDate()))
                .thenReturn(List.of(existingEntry));

        assertThatThrownBy(() -> activityEntryService.create(1L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_shouldThrowException_whenNewEntryFullyContainsExisting() {
        // existing: 09:00-11:00, new: 08:00-12:00 (fully wraps existing)
        request.setStartTime(LocalTime.of(8, 0));
        request.setEndTime(LocalTime.of(12, 0));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(activityType));
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(activitySubject));
        when(activityEntryRepository.findByUserIdAndActivityDate(1L, request.getActivityDate()))
                .thenReturn(List.of(existingEntry));

        assertThatThrownBy(() -> activityEntryService.create(1L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_shouldThrowException_whenNewEntryFullyInsideExisting() {
        // existing: 09:00-11:00, new: 09:30-10:30 (fully inside existing)
        request.setStartTime(LocalTime.of(9, 30));
        request.setEndTime(LocalTime.of(10, 30));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(activityType));
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(activitySubject));
        when(activityEntryRepository.findByUserIdAndActivityDate(1L, request.getActivityDate()))
                .thenReturn(List.of(existingEntry));

        assertThatThrownBy(() -> activityEntryService.create(1L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_shouldAllowSave_whenNewEntryStartsExactlyWhenExistingEnds() {
        // existing: 09:00-11:00, new: 11:00-12:00 (back-to-back, NOT an overlap)
        request.setStartTime(LocalTime.of(11, 0));
        request.setEndTime(LocalTime.of(12, 0));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(activityType));
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(activitySubject));
        when(activityEntryRepository.findByUserIdAndActivityDate(1L, request.getActivityDate()))
                .thenReturn(List.of(existingEntry));
        when(activityEntryRepository.save(any(ActivityEntry.class))).thenReturn(existingEntry);

        activityEntryService.create(1L, request);

        verify(activityEntryRepository, times(1)).save(any(ActivityEntry.class));
    }

    @Test
    void create_shouldAllowSave_whenNewEntryEndsExactlyWhenExistingStarts() {
        // existing: 09:00-11:00, new: 07:00-09:00 (back-to-back before, NOT an overlap)
        request.setStartTime(LocalTime.of(7, 0));
        request.setEndTime(LocalTime.of(9, 0));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(activityType));
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(activitySubject));
        when(activityEntryRepository.findByUserIdAndActivityDate(1L, request.getActivityDate()))
                .thenReturn(List.of(existingEntry));
        when(activityEntryRepository.save(any(ActivityEntry.class))).thenReturn(existingEntry);

        activityEntryService.create(1L, request);

        verify(activityEntryRepository, times(1)).save(any(ActivityEntry.class));
    }

    @Test
    void create_shouldAllowSave_whenNoExistingEntriesOnThatDate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(activityType));
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(activitySubject));
        when(activityEntryRepository.findByUserIdAndActivityDate(1L, request.getActivityDate()))
                .thenReturn(List.of());
        when(activityEntryRepository.save(any(ActivityEntry.class))).thenReturn(existingEntry);

        activityEntryService.create(1L, request);

        verify(activityEntryRepository, times(1)).save(any(ActivityEntry.class));
    }

    // ---------- getById ----------

    @Test
    void getById_shouldReturnResponse_whenRequestedByOwner() {
        when(activityEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry));

        ActivityEntryResponse response = activityEntryService.getById(1L, 1L, false);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTaskDescription()).isEqualTo("Working on backend API");
    }

    @Test
    void getById_shouldReturnResponse_whenRequestedByManagerWithTeamViewPermission() {
        when(activityEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry));

        ActivityEntryResponse response = activityEntryService.getById(1L, otherUserId, true);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getById_shouldThrowAccessDenied_whenRequestedByAnotherUserWithoutTeamView() {
        when(activityEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry));

        assertThatThrownBy(() -> activityEntryService.getById(1L, otherUserId, false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getById_shouldThrowException_whenEntryDoesNotExist() {
        when(activityEntryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityEntryService.getById(99L, 1L, false))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    // ---------- getByFilters: ownership scoping ----------

    @Test
    void getByFilters_shouldForceScopeToCaller_whenNoTeamViewAndNoUserIdGiven() {
        ActivityEntryFilter filter = new ActivityEntryFilter(null, null, null, null, null, null);

        when(activityEntryRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(existingEntry));

        List<ActivityEntryResponse> results = activityEntryService.getByFilters(filter, 1L, false);

        assertThat(results).hasSize(1);
    }

    @Test
    void getByFilters_shouldThrowAccessDenied_whenRequestingSomeoneElsesUserIdWithoutTeamView() {
        ActivityEntryFilter filter = new ActivityEntryFilter(otherUserId, null, null, null, null, null);

        assertThatThrownBy(() -> activityEntryService.getByFilters(filter, 1L, false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getByFilters_shouldAllowAnyUserId_whenCallerHasTeamViewPermission() {
        ActivityEntryFilter filter = new ActivityEntryFilter(otherUserId, null, null, null, null, null);

        when(activityEntryRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(existingEntry));

        List<ActivityEntryResponse> results = activityEntryService.getByFilters(filter, 1L, true);

        assertThat(results).hasSize(1);
    }

    @Test
    void getByFilters_shouldThrowException_whenFromDateAfterToDate() {
        ActivityEntryFilter filter = new ActivityEntryFilter(
                null, null, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 1), null, null);

        assertThatThrownBy(() -> activityEntryService.getByFilters(filter, 1L, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fromDate");
    }

    // ---------- update ----------

    @Test
    void update_shouldRecalculateDurationAndSave_whenOwnerRequests() {
        request.setStartTime(LocalTime.of(13, 0));
        request.setEndTime(LocalTime.of(15, 30));

        when(activityEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(activityType));
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(activitySubject));
        when(activityEntryRepository.findByUserIdAndActivityDate(1L, request.getActivityDate()))
                .thenReturn(List.of(existingEntry));
        when(activityEntryRepository.save(any(ActivityEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        ActivityEntryResponse response = activityEntryService.update(1L, request, 1L);

        assertThat(response.getDurationMinutes()).isEqualTo(150);
    }

    @Test
    void update_shouldThrowAccessDenied_whenRequestedByNonOwner() {
        when(activityEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry));

        assertThatThrownBy(() -> activityEntryService.update(1L, request, otherUserId))
                .isInstanceOf(AccessDeniedException.class);

        verify(activityEntryRepository, never()).save(any(ActivityEntry.class));
    }

    @Test
    void update_shouldThrowException_whenEntryDoesNotExist() {
        when(activityEntryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityEntryService.update(99L, request, 1L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(activityEntryRepository, never()).save(any(ActivityEntry.class));
    }

    @Test
    void update_shouldExcludeItselfFromOverlapCheck() {
        // updating the same entry with the same times should NOT be treated as an overlap
        when(activityEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(activityType));
        when(activitySubjectRepository.findById(1L)).thenReturn(Optional.of(activitySubject));
        when(activityEntryRepository.findByUserIdAndActivityDate(1L, request.getActivityDate()))
                .thenReturn(List.of(existingEntry));
        when(activityEntryRepository.save(any(ActivityEntry.class))).thenReturn(existingEntry);

        activityEntryService.update(1L, request, 1L);

        verify(activityEntryRepository, times(1)).save(any(ActivityEntry.class));
    }

    // ---------- delete ----------

    @Test
    void delete_shouldRemoveEntry_whenRequestedByOwner() {
        when(activityEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry));

        activityEntryService.delete(1L, 1L);

        verify(activityEntryRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_shouldThrowAccessDenied_whenRequestedByNonOwner() {
        when(activityEntryRepository.findById(1L)).thenReturn(Optional.of(existingEntry));

        assertThatThrownBy(() -> activityEntryService.delete(1L, otherUserId))
                .isInstanceOf(AccessDeniedException.class);

        verify(activityEntryRepository, never()).deleteById(any());
    }

    @Test
    void delete_shouldThrowException_whenEntryDoesNotExist() {
        when(activityEntryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityEntryService.delete(99L, 1L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(activityEntryRepository, never()).deleteById(any());
    }
}