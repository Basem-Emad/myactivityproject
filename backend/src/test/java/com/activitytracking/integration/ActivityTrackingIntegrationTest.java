package com.activitytracking.integration;

import com.activitytracking.activity.dto.request.ActivityEntryRequest;
import com.activitytracking.masterdata.entity.ActivitySubject;
import com.activitytracking.masterdata.entity.ActivityType;
import com.activitytracking.masterdata.entity.SubjectType;
import com.activitytracking.masterdata.repository.ActivitySubjectRepository;
import com.activitytracking.masterdata.repository.ActivityTypeRepository;
import com.activitytracking.user.dto.request.CreateUserRequestDto;
import com.activitytracking.user.dto.request.LoginRequestDto;
import com.activitytracking.user.dto.response.AuthResponseDto;
import com.activitytracking.user.entity.Permission;
import com.activitytracking.user.entity.Role;
import com.activitytracking.user.entity.User;
import com.activitytracking.user.repository.RoleRepository;
import com.activitytracking.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ActivityTrackingIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityTypeRepository activityTypeRepository;

    @Autowired
    private ActivitySubjectRepository activitySubjectRepository;

    @Autowired
    private com.activitytracking.user.repository.PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role adminRole;
    private Role employeeRole;
    private ActivityType projectType;
    private ActivitySubject subject;

    private static final String ADMIN_PASSWORD = "AdminPass123!";
    private static final String EMPLOYEE_PASSWORD = "EmployeePass123!";

    @BeforeEach
    void setUp() {
        // Fixtures are created directly through repositories, not through a mystery
        // seeded row: this test owns its own data and doesn't depend on migration
        // seed content (which the test has no known plaintext password for anyway).
        Set<Permission> allPermissions = Set.of(
                permission("USER_READ"), permission("USER_CREATE"),
                permission("USER_UPDATE"), permission("USER_DELETE"),
                permission("MASTERDATA_MANAGE"), permission("ACTIVITY_VIEW_TEAM")
        );

        adminRole = roleRepository.save(Role.builder()
                .name("IT_ADMIN_" + System.nanoTime())
                .permissions(allPermissions)
                .build());

        employeeRole = roleRepository.save(Role.builder()
                .name("IT_EMPLOYEE_" + System.nanoTime())
                .permissions(Set.of())
                .build());

        User admin = new User();
        admin.setName("Integration Admin");
        admin.setEmail("integration-admin@test.com");
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setRole(adminRole);
        admin.setActive(true);
        userRepository.save(admin);

        User employee = new User();
        employee.setName("Integration Employee");
        employee.setEmail("integration-employee@test.com");
        employee.setPassword(passwordEncoder.encode(EMPLOYEE_PASSWORD));
        employee.setRole(employeeRole);
        employee.setActive(true);
        userRepository.save(employee);

        projectType = activityTypeRepository.save(
                ActivityType.builder().name("IT-Project-" + System.nanoTime()).active(true).build());

        subject = activitySubjectRepository.save(
                ActivitySubject.builder()
                        .name("IT-Subject-" + System.nanoTime())
                        .subjectType(SubjectType.PROJECT)
                        .active(true)
                        .build());
    }

    private Permission permission(String name) {
        return permissionRepository.save(new Permission(null, name + "_" + System.nanoTime()));
    }

    @Test
    void unauthenticatedRequest_shouldBeRejected() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/users", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_shouldReturnWorkingAccessToken() {
        String accessToken = loginAndGetAccessToken("integration-admin@test.com", ADMIN_PASSWORD);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users", HttpMethod.GET, authenticated(accessToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void employee_shouldNotBeAbleToDeleteUsers_dueToMissingPermission() {
        String employeeToken = loginAndGetAccessToken("integration-employee@test.com", EMPLOYEE_PASSWORD);
        User admin = userRepository.findByEmail("integration-admin@test.com").orElseThrow();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users/" + admin.getId(), HttpMethod.DELETE,
                authenticated(employeeToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void admin_shouldBeAbleToCreateAndDeleteUsers() {
        String adminToken = loginAndGetAccessToken("integration-admin@test.com", ADMIN_PASSWORD);

        CreateUserRequestDto createRequest = new CreateUserRequestDto();
        createRequest.setName("New Hire");
        createRequest.setEmail("new-hire@test.com");
        createRequest.setPassword("Whatever123!");
        createRequest.setRoleId(employeeRole.getId());

        ResponseEntity<String> createResponse = restTemplate.exchange(
                "/api/v1/users", HttpMethod.POST,
                new HttpEntity<>(createRequest, authHeaders(adminToken)), String.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void employee_cannotViewAnotherEmployeesActivityEntries() {
        String employeeToken = loginAndGetAccessToken("integration-employee@test.com", EMPLOYEE_PASSWORD);
        User otherUser = userRepository.findByEmail("integration-admin@test.com").orElseThrow();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/activities?userId=" + otherUser.getId(), HttpMethod.GET,
                authenticated(employeeToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void employee_canCreateAndReadTheirOwnActivityEntry() {
        String employeeToken = loginAndGetAccessToken("integration-employee@test.com", EMPLOYEE_PASSWORD);

        ActivityEntryRequest entryRequest = new ActivityEntryRequest();
        entryRequest.setActivityDate(LocalDate.of(2026, 8, 27));
        entryRequest.setStartTime(LocalTime.of(9, 0));
        entryRequest.setEndTime(LocalTime.of(11, 0));
        entryRequest.setActivityTypeId(projectType.getId());
        entryRequest.setActivitySubjectId(subject.getId());
        entryRequest.setTaskDescription("Integration test entry");

        ResponseEntity<String> createResponse = restTemplate.exchange(
                "/api/v1/activities", HttpMethod.POST,
                new HttpEntity<>(entryRequest, authHeaders(employeeToken)), String.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> listResponse = restTemplate.exchange(
                "/api/v1/activities", HttpMethod.GET, authenticated(employeeToken), String.class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).contains("Integration test entry");
    }

    private String loginAndGetAccessToken(String email, String password) {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        ResponseEntity<AuthResponseDto> response = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, AuthResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().getAccessToken();
    }

    private HttpHeaders authHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    private HttpEntity<Void> authenticated(String accessToken) {
        return new HttpEntity<>(authHeaders(accessToken));
    }
}
