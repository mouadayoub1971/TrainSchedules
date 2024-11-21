package com.mouad.train.users;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class userLoginController {
    private final UserRepository repo;
    private final BCryptPasswordEncoder passwordEncoder;

    public userLoginController(UserRepository repo) {
        this.repo = repo;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return new ResponseEntity<>("Password is required", HttpStatus.BAD_REQUEST);
        }

        User existingUser = repo.findByEmail(user.getEmail());
        if (existingUser == null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setAdmin(false);
            repo.save(user);
            return new ResponseEntity<>("You were not found, but you have been registered. Welcome!", HttpStatus.CREATED);
        }

        if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            return new ResponseEntity<>("Invalid password", HttpStatus.UNAUTHORIZED);
        }

        if (existingUser.getAdmin()) {
            return new ResponseEntity<>("Welcome Admin", HttpStatus.OK);
        }

        return new ResponseEntity<>("Welcome User", HttpStatus.OK);
    }
}
