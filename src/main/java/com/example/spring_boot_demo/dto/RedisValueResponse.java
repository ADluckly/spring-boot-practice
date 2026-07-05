package com.example.spring_boot_demo.dto;

public record RedisValueResponse(String key, String value, boolean exists) {
}
