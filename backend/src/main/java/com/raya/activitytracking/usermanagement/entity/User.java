package com.raya.activitytracking.usermanagement.entity;

import com.raya.activitytracking.usermanagement.utilis.Gender;
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
//    @SequenceGenerator(
//            name = "user_Seq",
//            sequenceName = "user_Seq",
//            allocationSize = 1
//    )
//    @GeneratedValue(
//            strategy = GenerationType.SEQUENCE,
//            generator = "user_Seq"
//    )
    private Integer id;

    private String userName;
    @Column(nullable = false)
    private String password;
    private String email;
    private LocalDate dateOfBirth;
    private Gender gender;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;





   /* public User(String userName,
                String password,
                String email,
                LocalDate dateOfBirth,
                Gender gender) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }*/




}
