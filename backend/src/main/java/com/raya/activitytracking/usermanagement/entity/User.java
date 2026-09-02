package com.raya.activitytracking.usermanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.raya.activitytracking.usermanagement.util.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String userName;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(length = 100)
    private String email;

    private LocalDate dateOfBirth;

    private Gender gender;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}