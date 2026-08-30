package com.raya.activitytracking.usermanagement.repository;

import com.raya.activitytracking.usermanagement.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByEmail(String email);
    @EntityGraph(attributePaths = {
            "role",
            "role.permissions"
    })
    Optional<User> findByUserName(String userName);

    boolean existsByRoleId(Integer roleId);
}
