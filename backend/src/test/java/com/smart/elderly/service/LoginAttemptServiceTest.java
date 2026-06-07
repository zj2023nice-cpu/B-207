package com.smart.elderly.service;

import com.smart.elderly.common.SecurityConstants;
import com.smart.elderly.statemachine.LoginSecurityRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LoginAttemptService 登录尝试服务测试")
class LoginAttemptServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private LoginSecurityRules securityRules;

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    private static final String TEST_USERNAME = "testuser";
    private static final String ATTEMPT_KEY = "login:attempt:testuser";
    private static final String LOCK_KEY = "login:lock:testuser";

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(securityRules.buildAttemptKey(TEST_USERNAME)).thenReturn(ATTEMPT_KEY);
        when(securityRules.buildLockKey(TEST_USERNAME)).thenReturn(LOCK_KEY);
        when(securityRules.getAttemptExpireMinutes()).thenReturn(SecurityConstants.DEFAULT_ATTEMPT_EXPIRE_MINUTES);
        when(securityRules.getLockDurationMinutes()).thenReturn(SecurityConstants.DEFAULT_LOCK_DURATION_MINUTES);
        when(securityRules.getMaxAttempts()).thenReturn(SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS);
        when(securityRules.getCaptchaThreshold()).thenReturn(SecurityConstants.DEFAULT_CAPTCHA_THRESHOLD);
    }

    @Test
    @DisplayName("recordFailedAttempt - 首次失败应设置过期时间")
    void testRecordFailedAttempt_FirstAttempt_ShouldSetExpire() {
        when(valueOperations.increment(ATTEMPT_KEY)).thenReturn(1L);
        when(securityRules.shouldLock(1)).thenReturn(false);

        loginAttemptService.recordFailedAttempt(TEST_USERNAME);

        verify(valueOperations).increment(ATTEMPT_KEY);
        verify(stringRedisTemplate).expire(eq(ATTEMPT_KEY), eq(SecurityConstants.DEFAULT_ATTEMPT_EXPIRE_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("recordFailedAttempt - 非首次失败不应重复设置过期时间")
    void testRecordFailedAttempt_SubsequentAttempt_ShouldNotSetExpire() {
        when(valueOperations.increment(ATTEMPT_KEY)).thenReturn(2L);
        when(securityRules.shouldLock(2)).thenReturn(false);

        loginAttemptService.recordFailedAttempt(TEST_USERNAME);

        verify(valueOperations).increment(ATTEMPT_KEY);
        verify(stringRedisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("recordFailedAttempt - 达到锁定阈值时应锁定用户")
    void testRecordFailedAttempt_AtLockThreshold_ShouldLockUser() {
        int maxAttempts = SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS;
        when(valueOperations.increment(ATTEMPT_KEY)).thenReturn((long) maxAttempts);
        when(securityRules.shouldLock(maxAttempts)).thenReturn(true);

        loginAttemptService.recordFailedAttempt(TEST_USERNAME);

        verify(valueOperations).set(eq(LOCK_KEY), eq("locked"),
                eq(SecurityConstants.DEFAULT_LOCK_DURATION_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("recordFailedAttempt - 超过锁定阈值时应锁定用户")
    void testRecordFailedAttempt_AboveLockThreshold_ShouldLockUser() {
        int attempts = SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS + 5;
        when(valueOperations.increment(ATTEMPT_KEY)).thenReturn((long) attempts);
        when(securityRules.shouldLock(attempts)).thenReturn(true);

        loginAttemptService.recordFailedAttempt(TEST_USERNAME);

        verify(valueOperations).set(eq(LOCK_KEY), eq("locked"),
                eq(SecurityConstants.DEFAULT_LOCK_DURATION_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("recordFailedAttempt - increment返回null时不抛出异常")
    void testRecordFailedAttempt_NullIncrement_ShouldHandleGracefully() {
        when(valueOperations.increment(ATTEMPT_KEY)).thenReturn(null);

        assertDoesNotThrow(() -> loginAttemptService.recordFailedAttempt(TEST_USERNAME));
    }

    @Test
    @DisplayName("resetAttempts - 应删除尝试和锁定两个key")
    void testResetAttempts_ShouldDeleteBothKeys() {
        loginAttemptService.resetAttempts(TEST_USERNAME);

        verify(stringRedisTemplate).delete(ATTEMPT_KEY);
        verify(stringRedisTemplate).delete(LOCK_KEY);
    }

    @Test
    @DisplayName("isLocked - 锁定key存在时返回true")
    void testIsLocked_LockKeyExists_ShouldReturnTrue() {
        when(stringRedisTemplate.hasKey(LOCK_KEY)).thenReturn(true);

        assertTrue(loginAttemptService.isLocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("isLocked - 锁定key不存在时返回false")
    void testIsLocked_LockKeyNotExists_ShouldReturnFalse() {
        when(stringRedisTemplate.hasKey(LOCK_KEY)).thenReturn(false);

        assertFalse(loginAttemptService.isLocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("isLocked - hasKey返回null时返回false（安全处理）")
    void testIsLocked_HasKeyReturnsNull_ShouldReturnFalse() {
        when(stringRedisTemplate.hasKey(LOCK_KEY)).thenReturn(null);

        assertFalse(loginAttemptService.isLocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("requiresCaptcha - 无尝试记录时不需要验证码")
    void testRequiresCaptcha_NoAttempts_ShouldReturnFalse() {
        when(valueOperations.get(ATTEMPT_KEY)).thenReturn(null);

        assertFalse(loginAttemptService.requiresCaptcha(TEST_USERNAME));
        verify(securityRules, never()).requiresCaptcha(anyInt());
    }

    @Test
    @DisplayName("requiresCaptcha - 尝试次数低于阈值时不需要验证码")
    void testRequiresCaptcha_BelowThreshold_ShouldReturnFalse() {
        int attempts = SecurityConstants.DEFAULT_CAPTCHA_THRESHOLD - 1;
        when(valueOperations.get(ATTEMPT_KEY)).thenReturn(String.valueOf(attempts));
        when(securityRules.requiresCaptcha(attempts)).thenReturn(false);

        assertFalse(loginAttemptService.requiresCaptcha(TEST_USERNAME));
    }

    @Test
    @DisplayName("requiresCaptcha - 尝试次数达到阈值时需要验证码（边界值）")
    void testRequiresCaptcha_AtThreshold_ShouldReturnTrue() {
        int attempts = SecurityConstants.DEFAULT_CAPTCHA_THRESHOLD;
        when(valueOperations.get(ATTEMPT_KEY)).thenReturn(String.valueOf(attempts));
        when(securityRules.requiresCaptcha(attempts)).thenReturn(true);

        assertTrue(loginAttemptService.requiresCaptcha(TEST_USERNAME));
    }

    @Test
    @DisplayName("requiresCaptcha - 尝试次数超过阈值时需要验证码")
    void testRequiresCaptcha_AboveThreshold_ShouldReturnTrue() {
        int attempts = SecurityConstants.DEFAULT_CAPTCHA_THRESHOLD + 3;
        when(valueOperations.get(ATTEMPT_KEY)).thenReturn(String.valueOf(attempts));
        when(securityRules.requiresCaptcha(attempts)).thenReturn(true);

        assertTrue(loginAttemptService.requiresCaptcha(TEST_USERNAME));
    }

    @Test
    @DisplayName("getRemainingAttempts - 无尝试记录时返回最大次数")
    void testGetRemainingAttempts_NoAttempts_ShouldReturnMaxAttempts() {
        when(valueOperations.get(ATTEMPT_KEY)).thenReturn(null);
        when(securityRules.getMaxAttempts()).thenReturn(SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS);

        assertEquals(SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS,
                loginAttemptService.getRemainingAttempts(TEST_USERNAME));
    }

    @Test
    @DisplayName("getRemainingAttempts - 有尝试记录时委托securityRules计算")
    void testGetRemainingAttempts_WithAttempts_ShouldDelegateToRules() {
        int failedAttempts = 2;
        int expectedRemaining = 1;
        when(valueOperations.get(ATTEMPT_KEY)).thenReturn(String.valueOf(failedAttempts));
        when(securityRules.getRemainingAttempts(failedAttempts)).thenReturn(expectedRemaining);

        assertEquals(expectedRemaining, loginAttemptService.getRemainingAttempts(TEST_USERNAME));
    }

    @Test
    @DisplayName("getRemainingAttempts - 达到最大尝试次数时返回0")
    void testGetRemainingAttempts_AtMaxAttempts_ShouldReturnZero() {
        int maxAttempts = SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS;
        when(valueOperations.get(ATTEMPT_KEY)).thenReturn(String.valueOf(maxAttempts));
        when(securityRules.getRemainingAttempts(maxAttempts)).thenReturn(0);

        assertEquals(0, loginAttemptService.getRemainingAttempts(TEST_USERNAME));
    }

    @Test
    @DisplayName("recordFailedAttempt - 锁定阈值边界值测试（max-1不应锁定）")
    void testRecordFailedAttempt_BelowLockThreshold_ShouldNotLock() {
        int attempts = SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS - 1;
        when(valueOperations.increment(ATTEMPT_KEY)).thenReturn((long) attempts);
        when(securityRules.shouldLock(attempts)).thenReturn(false);

        loginAttemptService.recordFailedAttempt(TEST_USERNAME);

        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("recordFailedAttempt - 锁定阈值边界值测试（等于max应锁定）")
    void testRecordFailedAttempt_ExactlyAtMaxAttempts_ShouldLock() {
        int maxAttempts = SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS;
        when(valueOperations.increment(ATTEMPT_KEY)).thenReturn((long) maxAttempts);
        when(securityRules.shouldLock(maxAttempts)).thenReturn(true);

        loginAttemptService.recordFailedAttempt(TEST_USERNAME);

        verify(valueOperations).set(eq(LOCK_KEY), eq("locked"),
                eq(SecurityConstants.DEFAULT_LOCK_DURATION_MINUTES), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("组合场景 - 连续失败直至锁定的完整流程")
    void testFullLockScenario_ConsecutiveFailures_UntilLocked() {
        int maxAttempts = SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS;

        for (int i = 1; i <= maxAttempts; i++) {
            when(valueOperations.increment(ATTEMPT_KEY)).thenReturn((long) i);
            when(securityRules.shouldLock(i)).thenReturn(i >= maxAttempts);

            if (i == 1) {
                loginAttemptService.recordFailedAttempt(TEST_USERNAME);
                verify(stringRedisTemplate).expire(eq(ATTEMPT_KEY), anyLong(), eq(TimeUnit.MINUTES));
            } else {
                loginAttemptService.recordFailedAttempt(TEST_USERNAME);
            }

            if (i < maxAttempts) {
                assertFalse(loginAttemptService.isLocked(TEST_USERNAME),
                        "第" + i + "次失败后不应锁定");
            }
        }

        when(stringRedisTemplate.hasKey(LOCK_KEY)).thenReturn(true);
        assertTrue(loginAttemptService.isLocked(TEST_USERNAME),
                "达到" + maxAttempts + "次失败后应锁定");
    }

    @Test
    @DisplayName("组合场景 - 登录成功后重置所有尝试记录")
    void testResetAfterSuccessfulLogin_ShouldClearAllRecords() {
        when(valueOperations.get(ATTEMPT_KEY)).thenReturn(String.valueOf(SecurityConstants.DEFAULT_MAX_LOGIN_ATTEMPTS - 1));
        when(stringRedisTemplate.hasKey(LOCK_KEY)).thenReturn(true);
        when(securityRules.requiresCaptcha(anyInt())).thenReturn(true);

        assertTrue(loginAttemptService.requiresCaptcha(TEST_USERNAME));
        assertTrue(loginAttemptService.isLocked(TEST_USERNAME));

        loginAttemptService.resetAttempts(TEST_USERNAME);

        verify(stringRedisTemplate).delete(ATTEMPT_KEY);
        verify(stringRedisTemplate).delete(LOCK_KEY);
    }
}
