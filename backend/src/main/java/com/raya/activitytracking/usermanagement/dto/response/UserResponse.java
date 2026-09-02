package com.raya.activitytracking.usermanagement.dto.response;

import com.raya.activitytracking.usermanagement.util.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String userName;
    private String email;
    private LocalDate dateOfBirth;
    private Gender gender;
    private Long roleId;
    private String roleName;
    private boolean active;
}