package com.smart.elderly.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private static final String LOGIN_ATTEMPT_PREFIX = "login:attempt:";
    private static final String LOGIN_LOCK_PREFIX = "login:lock:";
    private static final int MAX_ATTEMPTS = 3;
    private static final int CAPTCHA_THRESHOLD = 2;
    private static final long LOCK_DURATION_MINUTES = 15;
    private static final long ATTEMPT_EXPIRE_MINUTES = 30;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void recordFailedAttempt(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        Long attempts = stringRedisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1) {
            stringRedisTemplate.expire(key, ATTEMPT_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            lockUser(username);
        }
    }

    public void resetAttempts(String username) {
        stringRedisTemplate.delete(LOGIN_ATTEMPT_PREFIX + username);
        stringRedisTemplate.delete(LOGIN_LOCK_PREFIX + username);
    }

    public boolean isLocked(String username) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(LOGIN_LOCK_PREFIX + username));
    }

    public boolean requiresCaptcha(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        String attemptsStr = stringRedisTemplate.opsForValue().get(key);
        if (attemptsStr == null) {
            return false;
        }
        int attempts = Integer.parseInt(attemptsStr);
        return attempts >= CAPTCHA_THRESHOLD;
    }

    private void lockUser(String username) {
        String key = LOGIN_LOCK_PREFIX + username;
        stringRedisTemplate.opsForValue().set(key, "locked", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
    }

    public int getRemainingAttempts(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        String attemptsStr = stringRedisTemplate.opsForValue().get(key);
        if (attemptsStr == null) {
            return MAX_ATTEMPTS;
        }
        int attempts = Integer.parseInt(attemptsStr);
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }
}
