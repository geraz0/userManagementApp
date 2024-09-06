package org.example.usermanagement2.controller;

import org.example.usermanagement2.model.User;
import org.example.usermanagement2.repository.UserRepository;
import org.example.usermanagement2.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.GrantedAuthority;


import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // Inject PasswordEncoder

    // Constructor-based injection
    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Endpoint for user registration (open to all)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user, BindingResult result) {  // BindingResult must be after @Valid User
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        System.out.println("Registering User: " + user.getUsername());

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    // Endpoint for getting current user details (accessible by USER)
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<User> user = userRepository.findByUsername(currentUsername);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        return ResponseEntity.ok(user);
    }

    // Update user details (only the authenticated user can update their own details)
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(@Valid @RequestBody User updatedUser, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<User> existingUser = userRepository.findByUsername(currentUsername);

        if (existingUser.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        // Update details (email, password, etc.)
        User userToUpdate = existingUser.get();
        userToUpdate.setEmail(updatedUser.getEmail());
        userToUpdate.setPassword(passwordEncoder.encode(updatedUser.getPassword())); // Ensure password is encoded
        userRepository.save(userToUpdate);

        return ResponseEntity.ok("User details updated successfully");
    }

    // Admin-only endpoint to get all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Admin-only endpoint to delete a user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Log the username and their roles
        System.out.println("Current Authenticated User: " + authentication.getName());
        System.out.println("Roles: ");
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            System.out.println(authority.getAuthority());
        }

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // Admin-only endpoint to update user info
    @PutMapping("/updateUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserByAdmin(@PathVariable Long id, @Valid @RequestBody User updatedUser, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        // Find the user by ID
        Optional<User> existingUser = userRepository.findById(id);

        if (existingUser.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        // Update the user details (admin can update username, email, password, role, etc.)
        User userToUpdate = existingUser.get();
        userToUpdate.setUsername(updatedUser.getUsername()); // Admin can update username
        userToUpdate.setEmail(updatedUser.getEmail()); // Admin can update Email
        userToUpdate.setPassword(passwordEncoder.encode(updatedUser.getPassword())); // Ensure password is encoded
        userToUpdate.setRole(updatedUser.getRole());  // Admin can update user role

        // Save updated user
        userRepository.save(userToUpdate);

        return ResponseEntity.ok("User updated successfully by Admin");
    }

}
