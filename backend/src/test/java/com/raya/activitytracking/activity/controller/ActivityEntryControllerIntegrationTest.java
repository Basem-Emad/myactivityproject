package com.raya.activitytracking.activity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.raya.activitytracking.activity.dto.request.ActivityEntryRequest;
import com.raya.activitytracking.activity.dto.response.ActivityEntryResponse;
import com.raya.activitytracking.activity.service.ActivityEntryService;
import com.raya.activitytracking.usermanagement.entity.Role;
import com.raya.activitytracking.usermanagement.entity.User;
import com.raya.activitytracking.usermanagement.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ActivityEntryControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private ActivityEntryService activityEntryService;

    @InjectMocks
    private ActivityEntryController controller;

    private ObjectMapper objectMapper;
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Role mockRole = new Role();
        mockRole.setId(1L);
        mockRole.setName("USER");
        mockRole.setPermissions(Collections.emptySet());

        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        mockUser.setUserName("testuser");
        mockUser.setEmail("testuser@example.com");
        mockUser.setPassword("encodedPassword");
        mockUser.setRole(mockRole);

        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/activities creates activity for authenticated user")
    void create_shouldReturnCreated() throws Exception {
        ActivityEntryRequest request = new ActivityEntryRequest(
                LocalDate.of(2026, 8, 20),
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                1L,
                2L,
                "Integration test task"
        );

        ActivityEntryResponse response = ActivityEntryResponse.builder()
                .id(100L)
                .userId(TEST_USER_ID)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes(120)
                .durationFormatted("2h 0m")
                .activityTypeId(1L)
                .activityTypeName("Development")
                .activitySubjectId(2L)
                .activitySubjectName("Project Raya")
                .taskDescription("Integration test task")
                .build();

        when(activityEntryService.create(eq(TEST_USER_ID), any(ActivityEntryRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                .andExpect(jsonPath("$.activityTypeName").value("Development"))
                .andExpect(jsonPath("$.durationFormatted").value("2h 0m"));
    }

    @Test
    @DisplayName("GET /api/activities returns activities for authenticated user")
    void getAll_shouldReturnUserActivities() throws Exception {
        ActivityEntryResponse item = ActivityEntryResponse.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .date(LocalDate.of(2026, 8, 20))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .durationMinutes(60)
                .durationFormatted("1h 0m")
                .activityTypeId(1L)
                .activityTypeName("Development")
                .activitySubjectId(1L)
                .activitySubjectName("Module")
                .taskDescription("Task")
                .build();

        when(activityEntryService.getByUser(TEST_USER_ID))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(TEST_USER_ID));
    }

    @Test
    @DisplayName("GET /api/activities/{id} returns single activity for authenticated user")
    void getById_shouldReturnActivity() throws Exception {
        ActivityEntryResponse item = ActivityEntryResponse.builder()
                .id(5L)
                .userId(TEST_USER_ID)
                .date(LocalDate.of(2026, 8, 20))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .durationMinutes(60)
                .durationFormatted("1h 0m")
                .activityTypeId(1L)
                .activityTypeName("Development")
                .activitySubjectId(1L)
                .activitySubjectName("Module")
                .taskDescription("Task")
                .build();

        when(activityEntryService.getById(5L, TEST_USER_ID))
                .thenReturn(item);

        mockMvc.perform(get("/api/activities/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID));
    }

    @Test
    @DisplayName("DELETE /api/activities/{id} deletes activity for authenticated user")
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/activities/5"))
                .andExpect(status().isNoContent());

        verify(activityEntryService).delete(5L, TEST_USER_ID);
    }
}
