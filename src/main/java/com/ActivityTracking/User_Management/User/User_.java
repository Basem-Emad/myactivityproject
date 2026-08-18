package com.ActivityTracking.User_Management.User;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table
public class User_ {
    @Id
    @SequenceGenerator(
            name = "user_Seq",
            sequenceName="user_Seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy =GenerationType.SEQUENCE ,
            generator ="user_Seq"
    )
    private Integer Id;
    private String userName;
    private String Password;
    private String Email;
    private LocalDate dateOfBirth;
    private Gender gender;

    public User_() {
    }

    public User_(Integer id,
                String userName,
                String password,
                String email,
                LocalDate dateOfBirth,
                Gender gender) {
        this.Id = id;
        this.userName = userName;
        this.Password = password;
        this.Email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }
    public User_(String userName,
                String password,
                String email,
                LocalDate dateOfBirth,
                Gender gender) {
        this.userName = userName;
        this.Password = password;
        this.Email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
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

    @Override
    public String toString() {
        return "User{" +
                "Id=" + Id +
                ", userName='" + userName + '\'' +
                ", Password='" + Password + '\'' +
                ", Email='" + Email + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender=" + gender +
                '}';
    }

}
