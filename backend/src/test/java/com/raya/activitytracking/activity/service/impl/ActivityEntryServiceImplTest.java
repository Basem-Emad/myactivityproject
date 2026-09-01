package com.raya.activitytracking.activity.service.impl;

import com.raya.activitytracking.activity.repository.ActivityEntryRepository;
import com.raya.activitytracking.masterdata.repository.ActivityTypeRepository;
import com.raya.activitytracking.masterdata.repository.ActivitySubjectRepository;
import com.raya.activitytracking.usermanagement.entity.User;
import com.raya.activitytracking.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import com.raya.activitytracking.activity.dto.request.ActivityEntryRequest;
import com.raya.activitytracking.activity.dto.response.ActivityEntryResponse;
import com.raya.activitytracking.activity.entity.ActivityEntry;
import com.raya.activitytracking.masterdata.entity.ActivitySubject;
import com.raya.activitytracking.masterdata.entity.ActivityType;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityEntryServiceImplTest {

    @Mock private ActivityEntryRepository activityEntryRepository;
    @Mock private ActivityTypeRepository activityTypeRepository;
    @Mock private ActivitySubjectRepository activitySubjectRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ActivityEntryServiceImpl service;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testuser");
    }

    // ─── GET BY ID ───────────────────────────────────────────

    @Test
    @DisplayName("getById: returns response when entry exists and belongs to requesting user")
    void getById_shouldReturnResponse_whenEntryBelongsToUser() {
        ActivityType type = ActivityType.builder().id(1L).name("Project").build();
        ActivitySubject subject = ActivitySubject.builder().id(2L).name("Dev").build();

        ActivityEntry entry = ActivityEntry.builder()
                .id(10L)
                .user(testUser)
                .date(LocalDate.of(2026, 8, 20))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 30))
                .durationMinutes(90)
                .activityType(type)
                .activitySubject(subject)
                .taskDescription("Investigating issue")
                .build();

        when(activityEntryRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(entry));

        ActivityEntryResponse response = service.getById(10L, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getDurationFormatted()).isEqualTo("1h 30m");
        assertThat(response.getActivityTypeName()).isEqualTo("Project");
        assertThat(response.getActivitySubjectName()).isEqualTo("Dev");
    }

    @Test
    @DisplayName("getById: throws EntityNotFoundException when entry does not belong to user or does not exist")
    void getById_shouldThrowException_whenEntryNotFoundOrBelongsToDifferentUser() {
        when(activityEntryRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Activity entry not found with id: 99");
    }

    // ─── DURATION CALCULATION ────────────────────────────────

    @Test
    @DisplayName("Duration: 3 hours (09:00 → 12:00) = 180 minutes")
    void calculateDuration_threeHours() {
        Integer result = service.calculateDuration(
                LocalTime.of(9, 0), LocalTime.of(12, 0));
        assertEquals(180, result);
    }

    @Test
    @DisplayName("Duration: 3.5 hours (09:00 → 12:30) = 210 minutes")
    void calculateDuration_threeAndHalfHours() {
        Integer result = service.calculateDuration(
                LocalTime.of(9, 0), LocalTime.of(12, 30));
        assertEquals(210, result);
    }

    @Test
    @DisplayName("Duration: 15 minutes (09:00 → 09:15) = 15 minutes")
    void calculateDuration_fifteenMinutes() {
        Integer result = service.calculateDuration(
                LocalTime.of(9, 0), LocalTime.of(9, 15));
        assertEquals(15, result);
    }

    @Test
    @DisplayName("Duration: 59 minutes (09:00 → 09:59) = 59 minutes")
    void calculateDuration_fiftyNineMinutes() {
        Integer result = service.calculateDuration(
                LocalTime.of(9, 0), LocalTime.of(9, 59));
        assertEquals(59, result);
    }

    @Test
    @DisplayName("Duration: full day (08:00 → 17:00) = 540 minutes")
    void calculateDuration_fullDay() {
        Integer result = service.calculateDuration(
                LocalTime.of(8, 0), LocalTime.of(17, 0));
        assertEquals(540, result);
    }

    // ─── CREATE VALIDATIONS ──────────────────────────────────

    @Test
    @DisplayName("create: should succeed when both ActivityType and ActivitySubject are active")
    void create_shouldSucceed_whenTypeAndSubjectAreActive() {
        ActivityEntryRequest request = new ActivityEntryRequest(
                LocalDate.of(2026, 8, 20),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                1L,
                2L,
                "Valid task"
        );

        ActivityType activeType = ActivityType.builder().id(1L).name("Development").active(true).build();
        ActivitySubject activeSubject = ActivitySubject.builder().id(2L).name("Project Raya").active(true).build();

        when(activityEntryRepository.findOverlapping(1L, request.getDate(), request.getStartTime(), request.getEndTime(), null))
                .thenReturn(java.util.Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(activeType));
        when(activitySubjectRepository.findById(2L)).thenReturn(Optional.of(activeSubject));
        when(activityEntryRepository.save(org.mockito.ArgumentMatchers.any(ActivityEntry.class)))
                .thenAnswer(inv -> {
                    ActivityEntry e = inv.getArgument(0);
                    e.setId(100L);
                    return e;
                });

        ActivityEntryResponse response = service.create(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getActivityTypeName()).isEqualTo("Development");
        assertThat(response.getActivitySubjectName()).isEqualTo("Project Raya");
    }

    @Test
    @DisplayName("create: should throw EntityNotFoundException when user is not found")
    void create_shouldThrowException_whenUserNotFound() {
        ActivityEntryRequest request = new ActivityEntryRequest(
                LocalDate.of(2026, 8, 20),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                1L,
                2L,
                "Valid task"
        );

        when(activityEntryRepository.findOverlapping(999L, request.getDate(), request.getStartTime(), request.getEndTime(), null))
                .thenReturn(java.util.Collections.emptyList());
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(999L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
    }

    @Test
    @DisplayName("create: should throw IllegalArgumentException when ActivityType is inactive")
    void create_shouldThrowException_whenActivityTypeIsInactive() {
        ActivityEntryRequest request = new ActivityEntryRequest(
                LocalDate.of(2026, 8, 20),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                1L,
                2L,
                "Valid task"
        );

        ActivityType inactiveType = ActivityType.builder().id(1L).name("Retired Type").active(false).build();
        ActivitySubject activeSubject = ActivitySubject.builder().id(2L).name("Project Raya").active(true).build();

        when(activityEntryRepository.findOverlapping(1L, request.getDate(), request.getStartTime(), request.getEndTime(), null))
                .thenReturn(java.util.Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(inactiveType));
        when(activitySubjectRepository.findById(2L)).thenReturn(Optional.of(activeSubject));

        assertThatThrownBy(() -> service.create(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot create activity with inactive Activity Type: Retired Type");
    }

    @Test
    @DisplayName("create: should throw IllegalArgumentException when ActivitySubject is inactive")
    void create_shouldThrowException_whenActivitySubjectIsInactive() {
        ActivityEntryRequest request = new ActivityEntryRequest(
                LocalDate.of(2026, 8, 20),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                1L,
                2L,
                "Valid task"
        );

        ActivityType activeType = ActivityType.builder().id(1L).name("Development").active(true).build();
        ActivitySubject inactiveSubject = ActivitySubject.builder().id(2L).name("Archived Subject").active(false).build();

        when(activityEntryRepository.findOverlapping(1L, request.getDate(), request.getStartTime(), request.getEndTime(), null))
                .thenReturn(java.util.Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(activeType));
        when(activitySubjectRepository.findById(2L)).thenReturn(Optional.of(inactiveSubject));

        assertThatThrownBy(() -> service.create(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot create activity with inactive Activity Subject: Archived Subject");
    }

    // ─── UPDATE VALIDATIONS ──────────────────────────────────

    @Test
    @DisplayName("update: should succeed when preserving existing inactive ActivityType and ActivitySubject")
    void update_shouldSucceed_whenPreservingExistingInactiveTypeAndSubject() {
        ActivityType existingInactiveType = ActivityType.builder().id(1L).name("Legacy Type").active(false).build();
        ActivitySubject existingInactiveSubject = ActivitySubject.builder().id(2L).name("Legacy Subject").active(false).build();

        ActivityEntry existingEntry = ActivityEntry.builder()
                .id(10L)
                .user(testUser)
                .date(LocalDate.of(2026, 8, 20))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .durationMinutes(60)
                .activityType(existingInactiveType)
                .activitySubject(existingInactiveSubject)
                .taskDescription("Old description")
                .build();

        ActivityEntryRequest updateRequest = new ActivityEntryRequest(
                LocalDate.of(2026, 8, 20),
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                1L, // same inactive type
                2L, // same inactive subject
                "Updated task description"
        );

        when(activityEntryRepository.findById(10L)).thenReturn(Optional.of(existingEntry));
        when(activityEntryRepository.findOverlapping(1L, updateRequest.getDate(), updateRequest.getStartTime(), updateRequest.getEndTime(), 10L))
                .thenReturn(java.util.Collections.emptyList());
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(existingInactiveType));
        when(activitySubjectRepository.findById(2L)).thenReturn(Optional.of(existingInactiveSubject));
        when(activityEntryRepository.save(org.mockito.ArgumentMatchers.any(ActivityEntry.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ActivityEntryResponse response = service.update(10L, 1L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTaskDescription()).isEqualTo("Updated task description");
        assertThat(response.getDurationMinutes()).isEqualTo(90);
    }

    @Test
    @DisplayName("update: should throw IllegalArgumentException when changing to a new inactive ActivityType")
    void update_shouldThrowException_whenChangingToInactiveType() {
        ActivityType existingType = ActivityType.builder().id(1L).name("Old Type").active(true).build();
        ActivitySubject existingSubject = ActivitySubject.builder().id(2L).name("Subject").active(true).build();

        ActivityEntry existingEntry = ActivityEntry.builder()
                .id(10L)
                .user(testUser)
                .activityType(existingType)
                .activitySubject(existingSubject)
                .build();

        ActivityType newInactiveType = ActivityType.builder().id(3L).name("Inactive Type").active(false).build();

        ActivityEntryRequest updateRequest = new ActivityEntryRequest(
                LocalDate.of(2026, 8, 20),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                3L, // changed to inactive
                2L,
                "Update attempt"
        );

        when(activityEntryRepository.findById(10L)).thenReturn(Optional.of(existingEntry));
        when(activityEntryRepository.findOverlapping(1L, updateRequest.getDate(), updateRequest.getStartTime(), updateRequest.getEndTime(), 10L))
                .thenReturn(java.util.Collections.emptyList());
        when(activityTypeRepository.findById(3L)).thenReturn(Optional.of(newInactiveType));
        when(activitySubjectRepository.findById(2L)).thenReturn(Optional.of(existingSubject));

        assertThatThrownBy(() -> service.update(10L, 1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot change to inactive Activity Type: Inactive Type");
    }

    @Test
    @DisplayName("update: should throw IllegalArgumentException when changing to a new inactive ActivitySubject")
    void update_shouldThrowException_whenChangingToInactiveSubject() {
        ActivityType existingType = ActivityType.builder().id(1L).name("Type").active(true).build();
        ActivitySubject existingSubject = ActivitySubject.builder().id(2L).name("Old Subject").active(true).build();

        ActivityEntry existingEntry = ActivityEntry.builder()
                .id(10L)
                .user(testUser)
                .activityType(existingType)
                .activitySubject(existingSubject)
                .build();

        ActivitySubject newInactiveSubject = ActivitySubject.builder().id(4L).name("Inactive Subject").active(false).build();

        ActivityEntryRequest updateRequest = new ActivityEntryRequest(
                LocalDate.of(2026, 8, 20),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                1L,
                4L, // changed to inactive
                "Update attempt"
        );

        when(activityEntryRepository.findById(10L)).thenReturn(Optional.of(existingEntry));
        when(activityEntryRepository.findOverlapping(1L, updateRequest.getDate(), updateRequest.getStartTime(), updateRequest.getEndTime(), 10L))
                .thenReturn(java.util.Collections.emptyList());
        when(activityTypeRepository.findById(1L)).thenReturn(Optional.of(existingType));
        when(activitySubjectRepository.findById(4L)).thenReturn(Optional.of(newInactiveSubject));

        assertThatThrownBy(() -> service.update(10L, 1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot change to inactive Activity Subject: Inactive Subject");
    }

    @Test
    @DisplayName("update: should throw IllegalArgumentException when trying to update another user's entry")
    void update_shouldThrowException_whenEntryBelongsToDifferentUser() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUserName("otheruser");

        ActivityEntry existingEntry = ActivityEntry.builder()
                .id(10L)
                .user(otherUser)
                .build();

        ActivityEntryRequest updateRequest = new ActivityEntryRequest(
                LocalDate.of(2026, 8, 20),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                1L,
                2L,
                "Update attempt"
        );

        when(activityEntryRepository.findById(10L)).thenReturn(Optional.of(existingEntry));

        assertThatThrownBy(() -> service.update(10L, 1L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You can only edit your own activity entries");
    }

    // ─── DELETE VALIDATIONS ──────────────────────────────────

    @Test
    @DisplayName("delete: should delete entry when it belongs to the requesting user")
    void delete_shouldSucceed_whenEntryBelongsToUser() {
        ActivityEntry existingEntry = ActivityEntry.builder()
                .id(10L)
                .user(testUser)
                .build();

        when(activityEntryRepository.findById(10L)).thenReturn(Optional.of(existingEntry));

        service.delete(10L, 1L);

        verify(activityEntryRepository).delete(existingEntry);
    }

    @Test
    @DisplayName("delete: should throw IllegalArgumentException when trying to delete another user's entry")
    void delete_shouldThrowException_whenEntryBelongsToDifferentUser() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUserName("otheruser");

        ActivityEntry existingEntry = ActivityEntry.builder()
                .id(10L)
                .user(otherUser)
                .build();

        when(activityEntryRepository.findById(10L)).thenReturn(Optional.of(existingEntry));

        assertThatThrownBy(() -> service.delete(10L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You can only delete your own activity entries");
    }
}
