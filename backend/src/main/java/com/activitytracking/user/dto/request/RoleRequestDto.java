package com.activitytracking.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class RoleRequestDto {

    @NotBlank(message = "Role name is required")
    private String name;

    @NotEmpty(message = "At least one permission id is required")
    private Set<Long> permissionIds;
}
