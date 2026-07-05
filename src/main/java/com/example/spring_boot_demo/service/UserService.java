package com.example.spring_boot_demo.service;

import java.util.NoSuchElementException;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_demo.dto.AuthResponse;
import com.example.spring_boot_demo.dto.LoginRequest;
import com.example.spring_boot_demo.dto.RegisterRequest;
import com.example.spring_boot_demo.dto.UpdateUserRequest;
import com.example.spring_boot_demo.dto.UserResponse;
import com.example.spring_boot_demo.model.AppUser;
import com.example.spring_boot_demo.repository.AppUserRepository;

@Service
@Transactional
public class UserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (appUserRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        AppUser user = new AppUser(
                request.username().trim(),
                request.email().trim().toLowerCase(),
                passwordEncoder.encode(request.password())
        );

        AppUser savedUser = appUserRepository.save(user);
        return toUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPasswordHash());
        if (!passwordMatches) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return new AuthResponse("Login successful", user.getId(), user.getUsername());
    }

    public void deleteUser(Long id) {
        if (!appUserRepository.existsById(id)) {
            throw new NoSuchElementException("User not found: " + id);
        }
        appUserRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return appUserRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + id));
        return toUserResponse(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + id));

        String normalizedUsername = request.username().trim();
        String normalizedEmail = request.email().trim().toLowerCase();

        if (appUserRepository.existsByUsernameAndIdNot(normalizedUsername, id)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (appUserRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.updateUsername(normalizedUsername);
        user.updateEmail(normalizedEmail);

        if (request.password() != null && !request.password().isBlank()) {
            user.updatePasswordHash(passwordEncoder.encode(request.password()));
        }

        AppUser updatedUser = appUserRepository.save(user);
        return toUserResponse(updatedUser);
    }

    private UserResponse toUserResponse(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }
}
