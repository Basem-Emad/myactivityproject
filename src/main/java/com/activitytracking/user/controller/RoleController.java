package com.activitytracking.user.controller;

import com.activitytracking.user.dto.request.RoleRequestDto;
import com.activitytracking.user.dto.response.PermissionResponseDto;
import com.activitytracking.user.dto.response.RoleResponseDto;
import com.activitytracking.user.constants.PermissionNames;
import com.activitytracking.user.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.USER_READ + "')")
    public ResponseEntity<List<RoleResponseDto>> listRoles() {
        return ResponseEntity.ok(roleService.listRoles());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.USER_CREATE + "')")
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody RoleRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.USER_UPDATE + "')")
    public ResponseEntity<RoleResponseDto> updateRole(
            @PathVariable Long id, @Valid @RequestBody RoleRequestDto request) {
        return ResponseEntity.ok(roleService.updateRole(id, request));
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('" + PermissionNames.USER_READ + "')")
    public ResponseEntity<List<PermissionResponseDto>> listPermissions() {
        return ResponseEntity.ok(roleService.listPermissions());
    }
}
