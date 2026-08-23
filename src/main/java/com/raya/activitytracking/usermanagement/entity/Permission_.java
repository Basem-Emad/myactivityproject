package com.raya.activitytracking.usermanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class Permission_ {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true)
    private String name;

    public Permission_(){}

    public Permission_(String name){
        this.name=name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
