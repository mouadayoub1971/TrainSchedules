package com.mouad.train.users;

import jakarta.persistence.*;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;

    private String firstName;
    private String secondName;

    @Column(unique = true)
    private String email;

    private Boolean admin;

    private String password;

    // Default constructor
    public User() {}

    // Constructor with parameters (without password)
    public User(Boolean admin, String email, String secondName, String firstName) {
        this.admin = admin;
        this.email = email;
        this.secondName = secondName;
        this.firstName = firstName;
    }

    // Constructor with all fields
    public User(Boolean admin, String email, String secondName, String firstName, String password) {
        this.admin = admin;
        this.email = email;
        this.secondName = secondName;
        this.firstName = firstName;
        this.password = password;
    }

    // Getters and setters
    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
