package com.ActivityTracking.User_Management.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User_,Integer> {
    Optional<User_> findUserByEmail(String email);

    Optional<User_> findById(Integer id);
}
