package com.raya.activitytracking.usermanagement.repository;

import com.raya.activitytracking.usermanagement.entity.User_;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User_,Integer> {
    Optional<User_> findByEmail(String email);

    Optional<User_> findByUserName(String userName);

    boolean existsByRoleId(Integer roleId);
}
