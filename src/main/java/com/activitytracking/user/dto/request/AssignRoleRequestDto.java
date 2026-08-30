package com.activitytracking.user.dto.request;

import com.activitytracking.user.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoleRequestDto {

    @NotNull(message = "Role is required")
    private Role role;
}