package com.activitytracking.user.service.impl;

import com.activitytracking.user.dto.request.RoleRequestDto;
import com.activitytracking.user.dto.response.PermissionResponseDto;
import com.activitytracking.user.dto.response.RoleResponseDto;
import com.activitytracking.user.entity.Permission;
import com.activitytracking.user.entity.Role;
import com.activitytracking.user.repository.PermissionRepository;
import com.activitytracking.user.repository.RoleRepository;
import com.activitytracking.user.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDto> listRoles() {
        return roleRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public RoleResponseDto createRole(RoleRequestDto request) {
        if (roleRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("A role with this name already exists");
        }

        Role role = Role.builder()
                .name(request.getName())
                .permissions(resolvePermissions(request.getPermissionIds()))
                .build();

        return toResponse(roleRepository.save(role));
    }

    @Override
    public RoleResponseDto updateRole(Long id, RoleRequestDto request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));

        roleRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("A role with this name already exists");
            }
        });

        role.setName(request.getName());
        role.setPermissions(resolvePermissions(request.getPermissionIds()));

        return toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponseDto> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionResponseDto(p.getId(), p.getName()))
                .toList();
    }

    private Set<Permission> resolvePermissions(Set<Long> permissionIds) {
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));

        if (permissions.size() != permissionIds.size()) {
            throw new IllegalArgumentException("One or more permission ids do not exist");
        }

        return permissions;
    }

    private RoleResponseDto toResponse(Role role) {
        List<String> permissionNames = role.getPermissions().stream()
                .map(Permission::getName)
                .sorted()
                .toList();

        return new RoleResponseDto(role.getId(), role.getName(), permissionNames);
    }
}
