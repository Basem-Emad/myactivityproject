package com.activitytracking.activity.specification;

import com.activitytracking.activity.entity.ActivityEntry;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ActivityEntrySpecification {

    public static Specification<ActivityEntry> withFilters(
            Long userId, LocalDate date, Long activityTypeId, Long activitySubjectId) {

        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();

            if (userId != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("user").get("id"), userId));
            }
            if (date != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("activityDate"), date));
            }
            if (activityTypeId != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("activityType").get("id"), activityTypeId));
            }
            if (activitySubjectId != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("activitySubject").get("id"), activitySubjectId));
            }

            return predicate;
        };
    }
}