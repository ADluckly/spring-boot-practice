package com.example.spring_boot_demo.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.spring_boot_demo.dto.RedisValueResponse;

@Service
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void setValue(String key, String value, Long ttlSeconds) {
        validateKey(key);

        if (ttlSeconds != null) {
            stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
            return;
        }

        stringRedisTemplate.opsForValue().set(key, value);
    }

    public RedisValueResponse getValue(String key) {
        validateKey(key);

        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return new RedisValueResponse(key, null, false);
        }

        return new RedisValueResponse(key, value, true);
    }

    public boolean deleteKey(String key) {
        validateKey(key);

        Boolean deleted = stringRedisTemplate.delete(key);
        return deleted != null && deleted;
    }

    private void validateKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("Key is required");
        }
    }
}
