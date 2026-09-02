package com.raya.activitytracking.usermanagement.service;

import com.raya.activitytracking.usermanagement.dto.request.RoleRequest;
import com.raya.activitytracking.usermanagement.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    List<RoleResponse> getRoles();
    RoleResponse getRoleById(Long id);
    RoleResponse addNewRole(RoleRequest request);
    void deleteRole(Long id);
    RoleResponse updateRole(Long id, RoleRequest updatedRole);
    RoleResponse assignPermissions(Long roleId, List<Long> permissionIds);
}