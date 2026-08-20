package com.raya.activitytracking.usermanagement.service;

import com.raya.activitytracking.usermanagement.entity.Role_;
import com.raya.activitytracking.usermanagement.entity.User_;
import com.raya.activitytracking.usermanagement.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    public List<Role_> getRoles(){
        return roleRepository.findAll();
    }
    public Role_ getRoleById(Integer id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }
}
