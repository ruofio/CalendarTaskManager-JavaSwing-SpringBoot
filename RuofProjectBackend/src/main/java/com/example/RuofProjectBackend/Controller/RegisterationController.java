package com.example.RuofProjectBackend.Controller;

import com.example.RuofProjectBackend.Model.MyAppUserRepository;
import com.example.RuofProjectBackend.Model.MyAppUser;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
public class RegisterationController {
    @Autowired
    private MyAppUserRepository myAppUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping(value = "/req/signup", consumes = "application/json")
    public ResponseEntity<Map<String, String>> createUser(@RequestBody MyAppUser user) {
        Map<String, String> response = new HashMap<>();

        // Check if username already exists
        Optional<MyAppUser> existingUserByUsername = myAppUserRepository.findByUsername(user.getUsername());
        if (existingUserByUsername.isPresent()) {
            response.put("error", "Username is already registered. Please change your username and try again.");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if email already exists
        Optional<MyAppUser> existingUserByEmail = myAppUserRepository.findByEmail(user.getEmail());
        if (existingUserByEmail.isPresent()) {
            response.put("error", "This Email is already registered. Please log in instead.");
            return ResponseEntity.badRequest().body(response);
        }

        // Check password length and at least 2 different characters
        if (user.getPassword().length() < 8) {
            response.put("error", "Password must be at least 8 characters long.");
            return ResponseEntity.badRequest().body(response);
        }

        // Regex to check for at least 2 different characters in the password
        if (!Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d).{8,}$").matcher(user.getPassword()).matches()) {
            response.put("error", "Password must contain at least 2 different characters (letters and digits).");
            return ResponseEntity.badRequest().body(response);
        }

        // Encrypt and save the new user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        myAppUserRepository.save(user);

        response.put("message", "User registered successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/req/login", consumes = "application/json")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody MyAppUser loginRequest) {
        Map<String, String> response = new HashMap<>();
        
        // Find user by username
        Optional<MyAppUser> user = myAppUserRepository.findByUsername(loginRequest.getUsername());
        
        // Check if user exists and password matches
        if (user.isEmpty() || !passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword())) {
            response.put("error", "Invalid username or password");
            return ResponseEntity.status(401).body(response);
        }
        
        // Return the userId and username
        response.put("message", "Login successful");
        response.put("userId", String.valueOf(user.get().getId()));  // Add the user ID to the response
        response.put("username", user.get().getUsername());  // Make sure to include the username
        return ResponseEntity.ok(response);
    }
    
 @GetMapping("/api/user/me")
public ResponseEntity<MyAppUser> getLoggedInUser(Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
        String username = authentication.getName();
        Optional<MyAppUser> user = myAppUserRepository.findByUsername(username);
        return user.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
}


}