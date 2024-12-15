package com.mouad.train.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private boolean admin;
    private String email;
    private String firstName;
    private String secondName;

    public boolean getAdmin() {
        return admin;
    }
}
