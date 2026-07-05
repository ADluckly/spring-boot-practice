package com.example.spring_boot_demo.dto;

import java.time.LocalDateTime;

public record UserResponse(Long id, String username, String email, LocalDateTime createdAt) {
}
