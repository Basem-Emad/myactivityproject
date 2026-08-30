package com.activitytracking.masterdata.dto.response;

import com.activitytracking.masterdata.entity.SubjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivitySubjectResponse {

    private Long id;
    private String name;
    private SubjectType subjectType;
    private String description;
    private Boolean active;
}