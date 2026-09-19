package com.activitytracking.integration;

import com.activitytracking.activity.dto.request.ActivityEntryRequest;
import com.activitytracking.activity.repository.ActivityEntryRepository;
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
import com.activitytracking.user.repository.PermissionRepository;
import com.activitytracking.user.repository.RoleRepository;
import com.activitytracking.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    @Value("${local.server.port}")
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityTypeRepository activityTypeRepository;

    @Autowired
    private ActivitySubjectRepository activitySubjectRepository;

    @Autowired
    private ActivityEntryRepository activityEntryRepository;

    @Autowired
    private PermissionRepository permissionRepository;

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
        // Permission names must match PermissionNames exactly (no suffix) since
        // @PreAuthorize checks the literal authority string.
        Set<Permission> allPermissions = Set.of(
                permission("USER_READ"), permission("USER_CREATE"),
                permission("USER_UPDATE"), permission("USER_DELETE"),
                permission("MASTERDATA_MANAGE"), permission("ACTIVITY_VIEW_TEAM")
        );

        adminRole = roleRepository.save(Role.builder()
                .name("IT_ADMIN")
                .permissions(allPermissions)
                .build());

        employeeRole = roleRepository.save(Role.builder()
                .name("IT_EMPLOYEE")
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
                ActivityType.builder().name("IT-Project").active(true).build());

        subject = activitySubjectRepository.save(
                ActivitySubject.builder()
                        .name("IT-Subject")
                        .subjectType(SubjectType.PROJECT)
                        .active(true)
                        .build());
    }

    @AfterEach
    void tearDown() {
        // Wipe everything this test class touches so each test starts from a clean
        // slate. Order matters for FK constraints: activity entries before users
        // (they reference users), users before roles (they reference roles),
        // and permissions last (after the role_permissions join rows are gone).
        activityEntryRepository.deleteAll();
        userRepository.deleteAll();
        activityTypeRepository.deleteAll();
        activitySubjectRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    private Permission permission(String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(new Permission(null, name)));
    }

    @Test
    void unauthenticatedRequest_shouldBeRejected() throws Exception {
        HttpResponse<String> response = sendGet("/api/v1/users", null);
        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void login_shouldReturnWorkingAccessToken() throws Exception {
        String accessToken = loginAndGetAccessToken("integration-admin@test.com", ADMIN_PASSWORD);

        HttpResponse<String> response = sendGet("/api/v1/users", accessToken);
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void employee_shouldNotBeAbleToDeleteUsers_dueToMissingPermission() throws Exception {
        String employeeToken = loginAndGetAccessToken("integration-employee@test.com", EMPLOYEE_PASSWORD);
        User admin = userRepository.findByEmail("integration-admin@test.com").orElseThrow();

        HttpResponse<String> response = sendDelete("/api/v1/users/" + admin.getId(), employeeToken);
        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    void admin_shouldBeAbleToCreateUsers() throws Exception {
        String adminToken = loginAndGetAccessToken("integration-admin@test.com", ADMIN_PASSWORD);

        CreateUserRequestDto createRequest = new CreateUserRequestDto();
        createRequest.setName("New Hire");
        createRequest.setEmail("new-hire@test.com");
        createRequest.setPassword("Whatever123!");
        createRequest.setRoleId(employeeRole.getId());

        HttpResponse<String> response = sendPost("/api/v1/users", createRequest, adminToken);
        assertThat(response.statusCode()).isEqualTo(201);
    }

    @Test
    void employee_cannotViewAnotherEmployeesActivityEntries() throws Exception {
        String employeeToken = loginAndGetAccessToken("integration-employee@test.com", EMPLOYEE_PASSWORD);
        User otherUser = userRepository.findByEmail("integration-admin@test.com").orElseThrow();

        HttpResponse<String> response = sendGet("/api/v1/activities?userId=" + otherUser.getId(), employeeToken);
        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    void employee_canCreateAndReadTheirOwnActivityEntry() throws Exception {
        String employeeToken = loginAndGetAccessToken("integration-employee@test.com", EMPLOYEE_PASSWORD);

        ActivityEntryRequest entryRequest = new ActivityEntryRequest();
        entryRequest.setActivityDate(LocalDate.of(2026, 8, 27));
        entryRequest.setStartTime(LocalTime.of(9, 0));
        entryRequest.setEndTime(LocalTime.of(11, 0));
        entryRequest.setActivityTypeId(projectType.getId());
        entryRequest.setActivitySubjectId(subject.getId());
        entryRequest.setTaskDescription("Integration test entry");

        HttpResponse<String> createResponse = sendPost("/api/v1/activities", entryRequest, employeeToken);
        assertThat(createResponse.statusCode()).isEqualTo(201);

        HttpResponse<String> listResponse = sendGet("/api/v1/activities", employeeToken);
        assertThat(listResponse.statusCode()).isEqualTo(200);
        assertThat(listResponse.body()).contains("Integration test entry");
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        HttpResponse<String> response = sendPost("/api/v1/auth/login", loginRequest, null);
        assertThat(response.statusCode()).isEqualTo(200);

        AuthResponseDto authResponse = objectMapper.readValue(response.body(), AuthResponseDto.class);
        assertThat(authResponse.getAccessToken()).isNotBlank();
        return authResponse.getAccessToken();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpResponse<String> sendGet(String path, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url(path))).GET();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPost(String path, Object body, String token) throws Exception {
        String json = objectMapper.writeValueAsString(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendDelete(String path, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url(path))).DELETE();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}