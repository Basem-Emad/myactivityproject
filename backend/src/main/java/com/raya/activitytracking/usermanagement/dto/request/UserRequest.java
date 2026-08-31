package com.raya.activitytracking.usermanagement.dto.request;


import com.raya.activitytracking.usermanagement.utilis.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String userName;
    private String password;
    private String email;
    private LocalDate dateOfBirth;
    private Gender gender;
    private Long roleId;
}
