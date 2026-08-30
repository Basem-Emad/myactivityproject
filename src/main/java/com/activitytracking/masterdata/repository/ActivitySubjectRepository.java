package com.activitytracking.masterdata.repository;

import com.activitytracking.masterdata.entity.ActivitySubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivitySubjectRepository extends JpaRepository<ActivitySubject, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<ActivitySubject> findByNameIgnoreCase(String name);
}