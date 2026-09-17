package com.activitytracking.activity.specification;

import com.activitytracking.activity.dto.request.ActivityEntryFilter;
import com.activitytracking.activity.entity.ActivityEntry;
import org.springframework.data.jpa.domain.Specification;

public class ActivityEntrySpecification {

    public static Specification<ActivityEntry> withFilters(ActivityEntryFilter filter) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();

            if (filter.userId() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("user").get("id"), filter.userId()));
            }

            // Single-date filter and from/to range filter are mutually exclusive by
            // convention (the service validates this); if both were somehow supplied,
            // the range takes precedence since it is the more specific request.
            if (filter.fromDate() != null || filter.toDate() != null) {
                if (filter.fromDate() != null) {
                    predicate = criteriaBuilder.and(predicate,
                            criteriaBuilder.greaterThanOrEqualTo(root.get("activityDate"), filter.fromDate()));
                }
                if (filter.toDate() != null) {
                    predicate = criteriaBuilder.and(predicate,
                            criteriaBuilder.lessThanOrEqualTo(root.get("activityDate"), filter.toDate()));
                }
            } else if (filter.date() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("activityDate"), filter.date()));
            }

            if (filter.activityTypeId() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("activityType").get("id"), filter.activityTypeId()));
            }
            if (filter.activitySubjectId() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("activitySubject").get("id"), filter.activitySubjectId()));
            }

            return predicate;
        };
    }
}
