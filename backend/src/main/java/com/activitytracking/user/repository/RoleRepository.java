package com.activitytracking.user.repository;

import com.activitytracking.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByNameIgnoreCase(String name);
}
