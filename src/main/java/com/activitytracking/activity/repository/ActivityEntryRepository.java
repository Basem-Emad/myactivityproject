package com.activitytracking.activity.repository;

import com.activitytracking.activity.entity.ActivityEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface ActivityEntryRepository
        extends JpaRepository<ActivityEntry, Long>, JpaSpecificationExecutor<ActivityEntry> {

    List<ActivityEntry> findByUserIdAndActivityDate(Long userId, LocalDate activityDate);

    List<ActivityEntry> findByUserIdAndActivityDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}