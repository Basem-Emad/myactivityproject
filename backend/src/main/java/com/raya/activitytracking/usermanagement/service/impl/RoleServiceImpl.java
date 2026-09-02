package com.raya.activitytracking.usermanagement.service.impl;

import com.raya.activitytracking.usermanagement.dto.request.RoleRequest;
import com.raya.activitytracking.usermanagement.dto.response.PermissionResponse;
import com.raya.activitytracking.usermanagement.dto.response.RoleResponse;
import com.raya.activitytracking.usermanagement.entity.Permission;
import com.raya.activitytracking.usermanagement.entity.Role;
import com.raya.activitytracking.usermanagement.repository.PermissionRepository;
import com.raya.activitytracking.usermanagement.repository.RoleRepository;
import com.raya.activitytracking.usermanagement.repository.UserRepository;
import com.raya.activitytracking.usermanagement.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        return toResponse(findEntityById(id));
    }

    @Override
    public RoleResponse addNewRole(RoleRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalStateException("Role already exists");
        }
        Role role = new Role();
        role.setName(request.getName());
        return toResponse(roleRepository.save(role));
    }

    @Override
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new EntityNotFoundException("Role with id " + id + " does not exist");
        }
        if (userRepository.existsByRoleId(id)) {
            throw new IllegalStateException(
                    "Cannot delete this role because there are users assigned to it");
        }
        roleRepository.deleteById(id);
    }

    @Override
    public RoleResponse updateRole(Long id, RoleRequest updatedRole) {
        Role existingRole = findEntityById(id);
        existingRole.setName(updatedRole.getName());
        return toResponse(roleRepository.save(existingRole));
    }

    @Override
    public RoleResponse assignPermissions(Long roleId, List<Long> permissionIds) {
        Role role = findEntityById(roleId);

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new EntityNotFoundException("One or more permissions not found");
        }

        role.setPermissions(new HashSet<>(permissions));
        return toResponse(roleRepository.save(role));
    }

    // ─── HELPERS ─────────────────────────────────────────────────

    private Role findEntityById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
    }

    private RoleResponse toResponse(Role role) {
        List<PermissionResponse> permissionResponses = role.getPermissions().stream()
                .map(p -> PermissionResponse.builder().id(p.getId()).name(p.getName()).build())
                .toList();

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(permissionResponses)
                .build();
    }
}