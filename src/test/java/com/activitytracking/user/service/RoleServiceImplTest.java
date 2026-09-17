package com.activitytracking.user.service;

import com.activitytracking.user.dto.request.RoleRequestDto;
import com.activitytracking.user.dto.response.PermissionResponseDto;
import com.activitytracking.user.dto.response.RoleResponseDto;
import com.activitytracking.user.entity.Permission;
import com.activitytracking.user.entity.Role;
import com.activitytracking.user.repository.PermissionRepository;
import com.activitytracking.user.repository.RoleRepository;
import com.activitytracking.user.service.impl.RoleServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Permission userRead;
    private Permission userCreate;
    private Role managerRole;
    private RoleRequestDto request;

    @BeforeEach
    void setUp() {
        userRead = new Permission(1L, "USER_READ");
        userCreate = new Permission(2L, "USER_CREATE");

        managerRole = Role.builder()
                .id(1L)
                .name("MANAGER")
                .permissions(Set.of(userRead))
                .build();

        request = new RoleRequestDto();
        request.setName("SUPERVISOR");
        request.setPermissionIds(Set.of(1L, 2L));
    }

    @Test
    void listRoles_shouldReturnMappedResponses() {
        when(roleRepository.findAll()).thenReturn(List.of(managerRole));

        List<RoleResponseDto> result = roleService.listRoles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("MANAGER");
        assertThat(result.get(0).getPermissions()).containsExactly("USER_READ");
    }

    @Test
    void createRole_shouldSaveAndReturnResponse_whenNameIsUnique() {
        when(roleRepository.existsByNameIgnoreCase("SUPERVISOR")).thenReturn(false);
        when(permissionRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(userRead, userCreate));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role toSave = invocation.getArgument(0);
            toSave.setId(10L);
            return toSave;
        });

        RoleResponseDto response = roleService.createRole(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("SUPERVISOR");
        assertThat(response.getPermissions()).containsExactlyInAnyOrder("USER_READ", "USER_CREATE");
    }

    @Test
    void createRole_shouldThrowException_whenNameAlreadyExists() {
        when(roleRepository.existsByNameIgnoreCase("SUPERVISOR")).thenReturn(true);

        assertThatThrownBy(() -> roleService.createRole(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void createRole_shouldThrowException_whenAPermissionIdDoesNotExist() {
        when(roleRepository.existsByNameIgnoreCase("SUPERVISOR")).thenReturn(false);
        when(permissionRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(userRead)); // only 1 of 2 found

        assertThatThrownBy(() -> roleService.createRole(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("do not exist");

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void updateRole_shouldUpdateNameAndPermissions_whenRoleExists() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(managerRole));
        when(roleRepository.findByName("SUPERVISOR")).thenReturn(Optional.empty());
        when(permissionRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(userRead, userCreate));
        when(roleRepository.save(any(Role.class))).thenReturn(managerRole);

        RoleResponseDto response = roleService.updateRole(1L, request);

        assertThat(response.getName()).isEqualTo("SUPERVISOR");
        assertThat(response.getPermissions()).containsExactlyInAnyOrder("USER_READ", "USER_CREATE");
    }

    @Test
    void updateRole_shouldThrowException_whenRoleDoesNotExist() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.updateRole(99L, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void updateRole_shouldThrowException_whenNewNameBelongsToADifferentRole() {
        Role otherRole = Role.builder().id(2L).name("SUPERVISOR").permissions(Set.of()).build();

        when(roleRepository.findById(1L)).thenReturn(Optional.of(managerRole));
        when(roleRepository.findByName("SUPERVISOR")).thenReturn(Optional.of(otherRole));

        assertThatThrownBy(() -> roleService.updateRole(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void listPermissions_shouldReturnAllPermissions() {
        when(permissionRepository.findAll()).thenReturn(List.of(userRead, userCreate));

        List<PermissionResponseDto> result = roleService.listPermissions();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PermissionResponseDto::getName)
                .containsExactlyInAnyOrder("USER_READ", "USER_CREATE");
    }
}
