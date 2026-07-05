package com.example.spring_boot_demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_boot_demo.dto.RedisOperationResponse;
import com.example.spring_boot_demo.dto.RedisSetRequest;
import com.example.spring_boot_demo.dto.RedisValueResponse;
import com.example.spring_boot_demo.service.RedisService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/redis")
@Validated
public class RedisController {

    private final RedisService redisService;

    public RedisController(RedisService redisService) {
        this.redisService = redisService;
    }

    @PostMapping
    public ResponseEntity<RedisOperationResponse> setValue(@Valid @RequestBody RedisSetRequest request) {
        redisService.setValue(request.key(), request.value(), request.ttlSeconds());
        return ResponseEntity.ok(new RedisOperationResponse("Redis value saved", request.key()));
    }

    @GetMapping
    public ResponseEntity<RedisValueResponse> getValue(@RequestParam String key) {
        return ResponseEntity.ok(redisService.getValue(key));
    }

    @DeleteMapping
    public ResponseEntity<RedisOperationResponse> deleteValue(@RequestParam String key) {
        boolean deleted = redisService.deleteKey(key);
        String message = deleted ? "Redis key deleted" : "Redis key not found";
        return ResponseEntity.ok(new RedisOperationResponse(message, key));
    }
}
