package com.raya.activitytracking.usermanagement.service;

import com.raya.activitytracking.usermanagement.entity.Permission_;
import com.raya.activitytracking.usermanagement.entity.Role_;
import com.raya.activitytracking.usermanagement.repository.PermissionRepository;
import com.raya.activitytracking.usermanagement.repository.RoleRepository;
import com.raya.activitytracking.usermanagement.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    public RoleService(RoleRepository roleRepository, UserRepository userRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
    }
    public List<Role_> getRoles(){
        return roleRepository.findAll();
    }
    public Role_ getRoleById(Integer id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }
    public void addNewRole(Role_ role) {
        Optional<Role_> roleByName= roleRepository.findByName(role.getName());
        if(roleByName.isPresent())
            throw new IllegalStateException("Role already exists");

        roleRepository.save(role);

    }
    public void deleteRole(Integer id) {
        boolean exists=roleRepository.existsById(id);
        if(!exists){
            throw new IllegalStateException("Role with id"+id+"does not exist");
        }
        if (userRepository.existsByRoleId(id)) {
            throw new RuntimeException(
                    "Cannot delete this role because there are users assigned to it"
            );
        }
        roleRepository.deleteById(id);
    }
    public Role_ updateRole(Integer id, Role_ updatedRole) {

        Role_ existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        existingRole.setName(updatedRole.getName());
        return roleRepository.save(existingRole);

}
    public Role_ assignPermissions(
            Integer roleId,
            List<Integer> permissionIds) {

        // Find the role
        Role_ role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new RuntimeException("Role not found"));

        // Find all permissions
        List<Permission_> permissions =
                permissionRepository.findAllById(permissionIds);

        // Make sure all requested permissions exist
        if (permissions.size() != permissionIds.size()) {
            throw new RuntimeException(
                    "One or more permissions not found"
            );
        }

        // Assign permissions to role
        role.setPermissions(
                new HashSet<>(permissions)
        );

        // Save role
        return roleRepository.save(role);
    }

}
