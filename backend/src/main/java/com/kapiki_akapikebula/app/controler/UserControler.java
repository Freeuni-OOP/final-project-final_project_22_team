package com.kapiki_akapikebula.app.controler;

import com.kapiki_akapikebula.app.model.User;
import com.kapiki_akapikebula.app.service.AuthResponse;
import com.kapiki_akapikebula.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserControler {
    @Autowired
    private UserService userService;
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User savedUser = userService.registerUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage()); //for  error
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        try {
            String token = userService.loginUser(user.getEmail(), user.getPasswordHash());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage()); //for error
        }
    }
}