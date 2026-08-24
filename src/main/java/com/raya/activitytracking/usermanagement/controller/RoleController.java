package com.raya.activitytracking.usermanagement.controller;

import com.raya.activitytracking.usermanagement.entity.Role_;
import com.raya.activitytracking.usermanagement.entity.User_;
import com.raya.activitytracking.usermanagement.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/role")
public class RoleController {
    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    @GetMapping()
    @PreAuthorize("hasAuthority('Read_Role')")
    public List<Role_> getRoles(){
        return roleService.getRoles();
    }
    @PostMapping
    @PreAuthorize("hasAuthority('Create_Role')")
    public void addNewRole(@RequestBody Role_ role){
        roleService.addNewRole(role);
    }
    @DeleteMapping(
            path = "{roleId}")
    @PreAuthorize("hasAuthority('Delete_Role')")
    public void deleteRole(
            @PathVariable("roleId")Integer Id){
        roleService.deleteRole(Id);
    }
    @PutMapping("/updateRole/{id}")
    @PreAuthorize("hasAuthority('Update_Role')")
    public Role_ updateRole(
            @PathVariable Integer id,
            @RequestBody Role_ updatedRole) {

        return roleService.updateRole(id, updatedRole);
    }
    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('Assign_Permissions')")
    public ResponseEntity<Role_> assignPermissions(
            @PathVariable Integer roleId,
            @RequestBody List<Integer> permissionIds) {

        return ResponseEntity.ok(
                roleService.assignPermissions(
                        roleId,
                        permissionIds
                )
        );
    }
}
