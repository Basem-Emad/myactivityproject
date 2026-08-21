package com.raya.activitytracking.activity.repository;

import com.raya.activitytracking.activity.entity.ActivityEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityEntryRepository extends JpaRepository<ActivityEntry, Long> {

    // Find a single entry scoped to a specific user
    Optional<ActivityEntry> findByIdAndUserId(Long id, Long userId);

    // Find all entries for a user on a specific date (for overlap checking)
    List<ActivityEntry> findByUserIdAndDate(Long userId, LocalDate date);

    // Find all entries for a user (for listing)
    List<ActivityEntry> findByUserIdOrderByDateDescStartTimeDesc(Long userId);

    // Find all entries for a user in a date range (for monthly filtering)
    List<ActivityEntry> findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
            Long userId, LocalDate startDate, LocalDate endDate);

    // Overlap detection: finds entries that conflict with a proposed time range
    // An overlap exists when: existing.startTime < proposedEndTime AND
    // existing.endTime > proposedStartTime
    @Query("""
                SELECT e FROM ActivityEntry e
                WHERE e.userId = :userId
                  AND e.date = :date
                  AND e.startTime < :endTime
                  AND e.endTime > :startTime
                  AND (:excludeId IS NULL OR e.id <> :excludeId)
            """)
    List<ActivityEntry> findOverlapping(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);
}
