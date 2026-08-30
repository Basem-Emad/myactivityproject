package com.activitytracking.masterdata.repository;

import com.activitytracking.masterdata.entity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivityTypeRepository extends JpaRepository<ActivityType, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<ActivityType> findByNameIgnoreCase(String name);
}