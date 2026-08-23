package com.raya.activitytracking.usermanagement.repository;

import com.raya.activitytracking.usermanagement.entity.Permission_;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission_, Integer> {
    Optional<Permission_> findByName(String name);

    boolean existsByName(String name);
}
