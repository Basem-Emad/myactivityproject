package com.activitytracking.user.service;

import com.activitytracking.user.dto.request.RoleRequestDto;
import com.activitytracking.user.dto.response.PermissionResponseDto;
import com.activitytracking.user.dto.response.RoleResponseDto;

import java.util.List;

public interface RoleService {
    List<RoleResponseDto> listRoles();
    RoleResponseDto createRole(RoleRequestDto request);
    RoleResponseDto updateRole(Long id, RoleRequestDto request);
    List<PermissionResponseDto> listPermissions();
}
