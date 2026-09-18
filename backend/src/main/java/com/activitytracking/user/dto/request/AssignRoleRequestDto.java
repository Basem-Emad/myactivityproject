package com.activitytracking.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoleRequestDto {

    @NotNull(message = "Role id is required")
    private Long roleId;
}