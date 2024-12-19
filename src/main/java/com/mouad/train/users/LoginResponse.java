package com.mouad.train.users;

public class LoginResponse {
    private String message;
    private boolean admin;
    private String email;
    private String firstName;
    private String secondName;
    private Integer userId;

    public LoginResponse() {}

    public LoginResponse(String message, boolean admin, String email, String firstName, String secondName, Integer userId) {
        this.message = message;
        this.admin = admin;
        this.email = email;
        this.firstName = firstName;
        this.secondName = secondName;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
