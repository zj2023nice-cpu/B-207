package com.smart.elderly.service;

import com.smart.elderly.statemachine.LoginSecurityRules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private LoginSecurityRules securityRules;

    public void recordFailedAttempt(String username) {
        String key = securityRules.buildAttemptKey(username);
        Long attempts = stringRedisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1) {
            stringRedisTemplate.expire(key, securityRules.getAttemptExpireMinutes(), TimeUnit.MINUTES);
        }
        if (attempts != null && securityRules.shouldLock(attempts.intValue())) {
            lockUser(username);
        }
    }

    public void resetAttempts(String username) {
        stringRedisTemplate.delete(securityRules.buildAttemptKey(username));
        stringRedisTemplate.delete(securityRules.buildLockKey(username));
    }

    public boolean isLocked(String username) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(securityRules.buildLockKey(username)));
    }

    public boolean requiresCaptcha(String username) {
        String key = securityRules.buildAttemptKey(username);
        String attemptsStr = stringRedisTemplate.opsForValue().get(key);
        if (attemptsStr == null) {
            return false;
        }
        int attempts = Integer.parseInt(attemptsStr);
        return securityRules.requiresCaptcha(attempts);
    }

    private void lockUser(String username) {
        String key = securityRules.buildLockKey(username);
        stringRedisTemplate.opsForValue().set(key, "locked", securityRules.getLockDurationMinutes(), TimeUnit.MINUTES);
    }

    public int getRemainingAttempts(String username) {
        String key = securityRules.buildAttemptKey(username);
        String attemptsStr = stringRedisTemplate.opsForValue().get(key);
        if (attemptsStr == null) {
            return securityRules.getMaxAttempts();
        }
        int attempts = Integer.parseInt(attemptsStr);
        return securityRules.getRemainingAttempts(attempts);
    }
}
