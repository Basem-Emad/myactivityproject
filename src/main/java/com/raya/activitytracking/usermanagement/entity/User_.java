package com.raya.activitytracking.usermanagement.entity;

import com.raya.activitytracking.usermanagement.utilis.Gender;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User_ {

    @Id
    @SequenceGenerator(
            name = "user_Seq",
            sequenceName = "user_Seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_Seq"
    )
    private Integer id;

    private String userName;
    @Column(nullable = false)
    private String password;
    private String email;
    private LocalDate dateOfBirth;
    private Gender gender;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role_ role;

    public User_() {
    }

    public User_(Integer id,
                 String userName,
                 String password,
                 String email,
                 LocalDate dateOfBirth,
                 Gender gender) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public User_(String userName,
                 String password,
                 String email,
                 LocalDate dateOfBirth,
                 Gender gender) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Role_ getRole() {
        return role;
    }

    public void setRole(Role_ role) {
        this.role = role;
    }
}
