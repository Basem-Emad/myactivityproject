package com.activitytracking.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RoleResponseDto {
    private Long id;
    private String name;
    private List<String> permissions;
}
