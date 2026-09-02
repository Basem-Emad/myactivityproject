package com.raya.activitytracking.usermanagement.controller;

import com.raya.activitytracking.usermanagement.dto.request.RoleRequest;
import com.raya.activitytracking.usermanagement.dto.response.RoleResponse;
import com.raya.activitytracking.usermanagement.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "api/v1/role")
@Tag(name = "Role & Permissions", description = "Role management & Assign permissions to role")
public class RoleController {

    private final RoleService roleService;

    @GetMapping()
    @Operation(summary = "List all roles")
    @PreAuthorize("hasAuthority('Read_Role')")
    public List<RoleResponse> getRoles() {
        return roleService.getRoles();
    }

    @PostMapping
    @Operation(summary = "Create a new role")
    @PreAuthorize("hasAuthority('Create_Role')")
    public RoleResponse addNewRole(@Valid @RequestBody RoleRequest request) {
        return roleService.addNewRole(request);
    }

    @DeleteMapping(path = "{roleId}")
    @Operation(summary = "Delete a role by ID")
    @PreAuthorize("hasAuthority('Delete_Role')")
    public void deleteRole(@PathVariable("roleId") Long id) {
        roleService.deleteRole(id);
    }

    @PutMapping("/updateRole/{id}")
    @Operation(summary = "Update an existing role")
    @PreAuthorize("hasAuthority('Update_Role')")
    public RoleResponse updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest updatedRole) {
        return roleService.updateRole(id, updatedRole);
    }

    @PutMapping("/{roleId}/permissions")
    @Operation(summary = "Assign permissions to a role")
    @PreAuthorize("hasAuthority('Assign_Permissions')")
    public ResponseEntity<RoleResponse> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        return ResponseEntity.ok(roleService.assignPermissions(roleId, permissionIds));
    }
}