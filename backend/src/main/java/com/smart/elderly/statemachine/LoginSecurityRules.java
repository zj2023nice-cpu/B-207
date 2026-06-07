package com.smart.elderly.statemachine;

import com.smart.elderly.common.SecurityConstants;
import org.springframework.stereotype.Component;

@Component
public class LoginSecurityRules {

    private final int maxAttempts;
    private final int captchaThreshold;
    private final long lockDurationMinutes;
    private final long attemptExpireMinutes;

    public LoginSecurityRules() {
        this.maxAttempts = SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS;
        this.captchaThreshold = SecurityConstants.DEFAULT_CAPTCHA_THRESHOLD;
        this.lockDurationMinutes = SecurityConstants.DEFAULT_LOCK_DURATION_MINUTES;
        this.attemptExpireMinutes = SecurityConstants.DEFAULT_ATTEMPT_EXPIRE_MINUTES;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getCaptchaThreshold() {
        return captchaThreshold;
    }

    public long getLockDurationMinutes() {
        return lockDurationMinutes;
    }

    public long getAttemptExpireMinutes() {
        return attemptExpireMinutes;
    }

    public boolean shouldLock(int failedAttempts) {
        return failedAttempts >= maxAttempts;
    }

    public boolean requiresCaptcha(int failedAttempts) {
        return failedAttempts >= captchaThreshold;
    }

    public int getRemainingAttempts(int failedAttempts) {
        return Math.max(0, maxAttempts - failedAttempts);
    }

    public String buildAttemptKey(String username) {
        return SecurityConstants.LOGIN_ATTEMPT_PREFIX + username;
    }

    public String buildLockKey(String username) {
        return SecurityConstants.LOGIN_LOCK_PREFIX + username;
    }
}
