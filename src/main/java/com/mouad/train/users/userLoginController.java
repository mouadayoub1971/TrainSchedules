package com.mouad.train.users;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class userLoginController {
    private final UserRepository repo ;

    public userLoginController(UserRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/login")
    public String login(
           @RequestBody User user) {
        if (user.getAdmin()) {
            User existingAdmin = repo.findByEmail(user.getEmail());
            if (existingAdmin != null && existingAdmin.getAdmin()) {
                return "welcome admin";
            } else {

                return "adminNotFound";
            }
        } else {
            User existingUser = repo.findByEmail(user.getEmail());
            if (existingUser != null) {
                return "welcome user";
            } else {
                user.setAdmin(false);
                repo.save(user);
                return "you are not exist willcom";
            }
        }
    }
}
