package com.raya.activitytracking.usermanagement.controller;

import com.raya.activitytracking.usermanagement.entity.Role;
import com.raya.activitytracking.usermanagement.service.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
@AllArgsConstructor
@RestController
@RequestMapping(path = "api/v1/role")
@Tag(
        name = "Role & Permissions",
        description = "Role management & Assign permissions to role"
)
public class RoleController {
    private final RoleService roleService;

    @GetMapping()
    @PreAuthorize("hasAuthority('Read_Role')")
    public List<Role> getRoles(){
        return roleService.getRoles();
    }
    @PostMapping
    @PreAuthorize("hasAuthority('Create_Role')")
    public void addNewRole(@RequestBody Role role){
        roleService.addNewRole(role);
    }
    @DeleteMapping(
            path = "{roleId}")
    @PreAuthorize("hasAuthority('Delete_Role')")
    public void deleteRole(
            @PathVariable("roleId")Long Id){
        roleService.deleteRole(Id);
    }
    @PutMapping("/updateRole/{id}")
    @PreAuthorize("hasAuthority('Update_Role')")
    public Role updateRole(
            @PathVariable Long id,
            @RequestBody Role updatedRole) {

        return roleService.updateRole(id, updatedRole);
    }
    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('Assign_Permissions')")
    public ResponseEntity<Role> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {

        return ResponseEntity.ok(
                roleService.assignPermissions(
                        roleId,
                        permissionIds
                )
        );
    }
}
