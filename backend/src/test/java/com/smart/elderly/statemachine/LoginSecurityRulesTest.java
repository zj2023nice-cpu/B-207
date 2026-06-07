package com.smart.elderly.statemachine;

import com.smart.elderly.common.SecurityConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoginSecurityRules 登录安全规则测试")
class LoginSecurityRulesTest {

    private LoginSecurityRules securityRules;

    @BeforeEach
    void setUp() {
        securityRules = new LoginSecurityRules();
    }

    @Test
    @DisplayName("默认配置值正确加载")
    void testDefaultConfiguration_ShouldMatchSecurityConstants() {
        assertEquals(SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS, securityRules.getMaxAttempts(),
                "最大尝试次数应与 SecurityConstants 一致");
        assertEquals(SecurityConstants.DEFAULT_CAPTCHA_THRESHOLD, securityRules.getCaptchaThreshold(),
                "验证码阈值应与 SecurityConstants 一致");
        assertEquals(SecurityConstants.DEFAULT_LOCK_DURATION_MINUTES, securityRules.getLockDurationMinutes(),
                "锁定时长应与 SecurityConstants 一致");
        assertEquals(SecurityConstants.DEFAULT_ATTEMPT_EXPIRE_MINUTES, securityRules.getAttemptExpireMinutes(),
                "尝试记录过期时长应与 SecurityConstants 一致");
    }

    @Test
    @DisplayName("shouldLock - 未达到最大尝试次数不应锁定")
    void testShouldLock_BelowMaxAttempts_ShouldReturnFalse() {
        int attemptsBelowThreshold = SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS - 1;
        assertFalse(securityRules.shouldLock(attemptsBelowThreshold),
                attemptsBelowThreshold + "次失败不应触发锁定");
    }

    @Test
    @DisplayName("shouldLock - 等于最大尝试次数应锁定（边界值）")
    void testShouldLock_AtMaxAttempts_ShouldReturnTrue() {
        int attemptsAtThreshold = SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS;
        assertTrue(securityRules.shouldLock(attemptsAtThreshold),
                attemptsAtThreshold + "次失败应触发锁定（边界值）");
    }

    @Test
    @DisplayName("shouldLock - 超过最大尝试次数应锁定")
    void testShouldLock_AboveMaxAttempts_ShouldReturnTrue() {
        int attemptsAboveThreshold = SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS + 5;
        assertTrue(securityRules.shouldLock(attemptsAboveThreshold),
                attemptsAboveThreshold + "次失败应触发锁定");
    }

    @Test
    @DisplayName("shouldLock - 零次失败不应锁定")
    void testShouldLock_ZeroAttempts_ShouldReturnFalse() {
        assertFalse(securityRules.shouldLock(0), "0次失败不应触发锁定");
    }

    @Test
    @DisplayName("requiresCaptcha - 未达到验证码阈值不需要验证码")
    void testRequiresCaptcha_BelowThreshold_ShouldReturnFalse() {
        int attemptsBelowThreshold = SecurityConstants.DEFAULT_CAPTCHA_THRESHOLD - 1;
        assertFalse(securityRules.requiresCaptcha(attemptsBelowThreshold),
                attemptsBelowThreshold + "次失败不需要验证码");
    }

    @Test
    @DisplayName("requiresCaptcha - 等于验证码阈值需要验证码（边界值）")
    void testRequiresCaptcha_AtThreshold_ShouldReturnTrue() {
        int attemptsAtThreshold = SecurityConstants.DEFAULT_CAPTCHA_THRESHOLD;
        assertTrue(securityRules.requiresCaptcha(attemptsAtThreshold),
                attemptsAtThreshold + "次失败需要验证码（边界值）");
    }

    @Test
    @DisplayName("requiresCaptcha - 超过验证码阈值需要验证码")
    void testRequiresCaptcha_AboveThreshold_ShouldReturnTrue() {
        int attemptsAboveThreshold = SecurityConstants.DEFAULT_CAPTCHA_THRESHOLD + 3;
        assertTrue(securityRules.requiresCaptcha(attemptsAboveThreshold),
                attemptsAboveThreshold + "次失败需要验证码");
    }

    @Test
    @DisplayName("getRemainingAttempts - 零次失败时剩余次数等于最大次数")
    void testGetRemainingAttempts_ZeroAttempts_ShouldReturnMaxAttempts() {
        assertEquals(SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS,
                securityRules.getRemainingAttempts(0),
                "0次失败时剩余次数应等于最大尝试次数");
    }

    @Test
    @DisplayName("getRemainingAttempts - 部分失败时正确计算剩余次数")
    void testGetRemainingAttempts_PartialAttempts_ShouldCalculateCorrectly() {
        int failedAttempts = 1;
        int expectedRemaining = SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS - failedAttempts;
        assertEquals(expectedRemaining, securityRules.getRemainingAttempts(failedAttempts),
                failedAttempts + "次失败后剩余次数计算错误");
    }

    @Test
    @DisplayName("getRemainingAttempts - 达到最大次数时剩余为零（边界值）")
    void testGetRemainingAttempts_AtMaxAttempts_ShouldReturnZero() {
        assertEquals(0, securityRules.getRemainingAttempts(SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS),
                "达到最大尝试次数时剩余次数应为0");
    }

    @Test
    @DisplayName("getRemainingAttempts - 超过最大次数时返回零而非负数")
    void testGetRemainingAttempts_AboveMaxAttempts_ShouldReturnZero() {
        int attemptsAboveMax = SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS + 10;
        assertEquals(0, securityRules.getRemainingAttempts(attemptsAboveMax),
                "超过最大尝试次数后剩余次数不应为负数");
    }

    @Test
    @DisplayName("buildAttemptKey - 正确构建尝试记录键")
    void testBuildAttemptKey_ShouldContainPrefixAndUsername() {
        String username = "testuser";
        String key = securityRules.buildAttemptKey(username);
        assertTrue(key.startsWith(SecurityConstants.LOGIN_ATTEMPT_PREFIX),
                "尝试记录键应以正确前缀开头");
        assertTrue(key.contains(username),
                "尝试记录键应包含用户名");
        assertEquals(SecurityConstants.LOGIN_ATTEMPT_PREFIX + username, key,
                "尝试记录键格式不正确");
    }

    @Test
    @DisplayName("buildLockKey - 正确构建锁定键")
    void testBuildLockKey_ShouldContainPrefixAndUsername() {
        String username = "testuser";
        String key = securityRules.buildLockKey(username);
        assertTrue(key.startsWith(SecurityConstants.LOGIN_LOCK_PREFIX),
                "锁定键应以正确前缀开头");
        assertTrue(key.contains(username),
                "锁定键应包含用户名");
        assertEquals(SecurityConstants.LOGIN_LOCK_PREFIX + username, key,
                "锁定键格式不正确");
    }

    @Test
    @DisplayName("buildAttemptKey - 空用户名也能正确构建键")
    void testBuildAttemptKey_EmptyUsername_ShouldBuildKey() {
        String key = securityRules.buildAttemptKey("");
        assertEquals(SecurityConstants.LOGIN_ATTEMPT_PREFIX, key,
                "空用户名时尝试记录键应仅为前缀");
    }

    @Test
    @DisplayName("buildLockKey - 空用户名也能正确构建键")
    void testBuildLockKey_EmptyUsername_ShouldBuildKey() {
        String key = securityRules.buildLockKey("");
        assertEquals(SecurityConstants.LOGIN_LOCK_PREFIX, key,
                "空用户名时锁定键应仅为前缀");
    }
}
