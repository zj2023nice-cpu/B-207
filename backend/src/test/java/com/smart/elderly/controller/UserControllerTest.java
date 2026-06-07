package com.smart.elderly.controller;

import com.smart.elderly.common.Result;
import com.smart.elderly.common.SecurityConstants;
import com.smart.elderly.dto.LoginRequest;
import com.smart.elderly.entity.User;
import com.smart.elderly.service.CaptchaService;
import com.smart.elderly.service.LoginAttemptService;
import com.smart.elderly.service.UserService;
import com.smart.elderly.service.UserSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserController 用户控制器测试 - 登录安全分支覆盖")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private CaptchaService captchaService;

    @Mock
    private UserSessionService userSessionService;

    @InjectMocks
    private UserController userController;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String CORRECT_CAPTCHA = "abcd";
    private static final String CAPTCHA_UUID = "test-uuid";

    private MockHttpServletRequest httpRequest;
    private MockHttpSession session;

    private LoginRequest createLoginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private LoginRequest createLoginRequestWithCaptcha(String username, String password,
                                                        String captchaUuid, String captchaCode) {
        LoginRequest request = createLoginRequest(username, password);
        request.setCaptchaUuid(captchaUuid);
        request.setCaptchaCode(captchaCode);
        return request;
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1);
        user.setUsername(TEST_USERNAME);
        user.setDisplayName("测试用户");
        user.setPhone("13800138000");
        user.setRole("admin");
        user.setLastLoginTime(LocalDateTime.now());
        return user;
    }

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        httpRequest = new MockHttpServletRequest();
        httpRequest.setSession(session);
    }

    @Test
    @DisplayName("登录成功 - 正常登录流程，无验证码")
    void testLogin_Success_NoCaptchaNeeded() {
        LoginRequest request = createLoginRequest(TEST_USERNAME, TEST_PASSWORD);
        User testUser = createTestUser();

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(false);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(false);
        when(userService.login(TEST_USERNAME, TEST_PASSWORD)).thenReturn(testUser);

        Result<Map<String, Object>> result = userController.login(request, httpRequest);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        Map<String, Object> userInfo = result.getData();
        assertEquals(TEST_USERNAME, userInfo.get("username"));
        assertEquals("测试用户", userInfo.get("displayName"));

        verify(loginAttemptService).resetAttempts(TEST_USERNAME);
        verify(userSessionService).recordLoginSession(eq(testUser), any());
        assertNotNull(session.getAttribute(SecurityConstants.SESSION_USER_KEY));
    }

    @Test
    @DisplayName("登录失败 - 账户已锁定，直接拒绝")
    void testLogin_AccountLocked_ShouldReturnError() {
        LoginRequest request = createLoginRequest(TEST_USERNAME, TEST_PASSWORD);

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(true);

        Result<Map<String, Object>> result = userController.login(request, httpRequest);

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("账户已被锁定"));

        verify(userService, never()).login(anyString(), anyString());
        verify(loginAttemptService, never()).recordFailedAttempt(anyString());
    }

    @Test
    @DisplayName("登录失败 - 需要验证码但未提供")
    void testLogin_RequiresCaptchaButNotProvided_ShouldReturnError() {
        LoginRequest request = createLoginRequest(TEST_USERNAME, TEST_PASSWORD);

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(false);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(true);

        Result<Map<String, Object>> result = userController.login(request, httpRequest);

        assertEquals(500, result.getCode());
        assertEquals("请输入验证码", result.getMessage());

        verify(userService, never()).login(anyString(), anyString());
        verify(loginAttemptService, never()).recordFailedAttempt(anyString());
    }

    @Test
    @DisplayName("登录失败 - 需要验证码且验证码错误")
    void testLogin_RequiresCaptchaButWrong_ShouldReturnError() {
        LoginRequest request = createLoginRequestWithCaptcha(
                TEST_USERNAME, TEST_PASSWORD, CAPTCHA_UUID, "wrong");

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(false);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(true);
        when(captchaService.validateCaptcha(CAPTCHA_UUID, "wrong")).thenReturn(false);

        Result<Map<String, Object>> result = userController.login(request, httpRequest);

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("验证码错误或已过期"));

        verify(userService, never()).login(anyString(), anyString());
        verify(loginAttemptService, never()).recordFailedAttempt(anyString());
    }

    @Test
    @DisplayName("登录成功 - 需要验证码且验证码正确")
    void testLogin_RequiresCaptchaAndCorrect_ShouldSucceed() {
        LoginRequest request = createLoginRequestWithCaptcha(
                TEST_USERNAME, TEST_PASSWORD, CAPTCHA_UUID, CORRECT_CAPTCHA);
        User testUser = createTestUser();

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(false);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(true);
        when(captchaService.validateCaptcha(CAPTCHA_UUID, CORRECT_CAPTCHA)).thenReturn(true);
        when(userService.login(TEST_USERNAME, TEST_PASSWORD)).thenReturn(testUser);

        Result<Map<String, Object>> result = userController.login(request, httpRequest);

        assertEquals(200, result.getCode());
        verify(loginAttemptService).resetAttempts(TEST_USERNAME);
    }

    @Test
    @DisplayName("登录失败 - 用户名或密码错误，还有剩余机会，不需要验证码")
    void testLogin_WrongPassword_StillAttemptsLeft_NoCaptcha() {
        LoginRequest request = createLoginRequest(TEST_USERNAME, "wrongpass");

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(false);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(false);
        when(userService.login(TEST_USERNAME, "wrongpass")).thenReturn(null);
        when(loginAttemptService.getRemainingAttempts(TEST_USERNAME)).thenReturn(2);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(false);

        Result<Map<String, Object>> result = userController.login(request, httpRequest);

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("用户名或密码错误"));
        assertTrue(result.getMessage().contains("剩余2次机会"));
        assertFalse(result.getMessage().contains("验证码"));

        verify(loginAttemptService).recordFailedAttempt(TEST_USERNAME);
        verify(loginAttemptService, never()).resetAttempts(anyString());
    }

    @Test
    @DisplayName("登录失败 - 用户名或密码错误，还有剩余机会，需要验证码")
    void testLogin_WrongPassword_StillAttemptsLeft_NeedsCaptcha() {
        LoginRequest request = createLoginRequest(TEST_USERNAME, "wrongpass");

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(false);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(false, true);
        when(userService.login(TEST_USERNAME, "wrongpass")).thenReturn(null);
        when(loginAttemptService.getRemainingAttempts(TEST_USERNAME)).thenReturn(1);

        Result<Map<String, Object>> result = userController.login(request, httpRequest);

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("用户名或密码错误"));
        assertTrue(result.getMessage().contains("剩余1次机会"));
        assertTrue(result.getMessage().contains("请输入验证码"));

        verify(loginAttemptService).recordFailedAttempt(TEST_USERNAME);
    }

    @Test
    @DisplayName("登录失败 - 登录失败次数过多，账户被锁定")
    void testLogin_WrongPassword_NoAttemptsLeft_AccountLocked() {
        LoginRequest request = createLoginRequest(TEST_USERNAME, "wrongpass");

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(false);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(false);
        when(userService.login(TEST_USERNAME, "wrongpass")).thenReturn(null);
        when(loginAttemptService.getRemainingAttempts(TEST_USERNAME)).thenReturn(0);

        Result<Map<String, Object>> result = userController.login(request, httpRequest);

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("登录失败次数过多"));
        assertTrue(result.getMessage().contains("账户已被锁定"));

        verify(loginAttemptService).recordFailedAttempt(TEST_USERNAME);
    }

    @Test
    @DisplayName("登录成功后 - 重置失败尝试记录")
    void testLogin_Success_ShouldResetAttempts() {
        LoginRequest request = createLoginRequest(TEST_USERNAME, TEST_PASSWORD);
        User testUser = createTestUser();

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(false);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(false);
        when(userService.login(TEST_USERNAME, TEST_PASSWORD)).thenReturn(testUser);

        userController.login(request, httpRequest);

        verify(loginAttemptService).resetAttempts(TEST_USERNAME);
    }

    @Test
    @DisplayName("登录成功后 - 创建用户会话")
    void testLogin_Success_ShouldRecordSession() {
        LoginRequest request = createLoginRequest(TEST_USERNAME, TEST_PASSWORD);
        User testUser = createTestUser();

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(false);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(false);
        when(userService.login(TEST_USERNAME, TEST_PASSWORD)).thenReturn(testUser);

        userController.login(request, httpRequest);

        verify(userSessionService).recordLoginSession(eq(testUser), any());
    }

    @Test
    @DisplayName("登录成功后 - Session设置正确的用户信息")
    void testLogin_Success_ShouldSetSessionAttribute() {
        LoginRequest request = createLoginRequest(TEST_USERNAME, TEST_PASSWORD);
        User testUser = createTestUser();

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(false);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(false);
        when(userService.login(TEST_USERNAME, TEST_PASSWORD)).thenReturn(testUser);

        userController.login(request, httpRequest);

        User sessionUser = (User) session.getAttribute(SecurityConstants.SESSION_USER_KEY);
        assertNotNull(sessionUser);
        assertEquals(TEST_USERNAME, sessionUser.getUsername());
        assertEquals(SecurityConstants.DEFAULT_SESSION_TIMEOUT_SECONDS, session.getMaxInactiveInterval());
    }

    @Test
    @DisplayName("登录成功后 - 返回正确的用户信息字段")
    void testLogin_Success_ShouldReturnCorrectUserInfo() {
        LoginRequest request = createLoginRequest(TEST_USERNAME, TEST_PASSWORD);
        User testUser = createTestUser();

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(false);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(false);
        when(userService.login(TEST_USERNAME, TEST_PASSWORD)).thenReturn(testUser);

        Result<Map<String, Object>> result = userController.login(request, httpRequest);

        Map<String, Object> userInfo = result.getData();
        assertNotNull(userInfo);
        assertTrue(userInfo.containsKey("id"));
        assertTrue(userInfo.containsKey("username"));
        assertTrue(userInfo.containsKey("displayName"));
        assertTrue(userInfo.containsKey("phone"));
        assertTrue(userInfo.containsKey("role"));
        assertTrue(userInfo.containsKey("lastLoginTime"));
    }

    @Test
    @DisplayName("风险状态 - 账户锁定优先级最高，跳过其他检查")
    void testLogin_LockedAccount_TakesHighestPriority() {
        LoginRequest request = createLoginRequestWithCaptcha(
                TEST_USERNAME, TEST_PASSWORD, CAPTCHA_UUID, CORRECT_CAPTCHA);

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(true);

        Result<Map<String, Object>> result = userController.login(request, httpRequest);

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("账户已被锁定"));

        verify(loginAttemptService, never()).requiresCaptcha(anyString());
        verify(captchaService, never()).validateCaptcha(anyString(), anyString());
        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    @DisplayName("风险状态 - 需要验证码时跳过密码验证直到验证码通过")
    void testLogin_RequiresCaptcha_ValidatesCaptchaBeforePassword() {
        LoginRequest request = createLoginRequestWithCaptcha(
                TEST_USERNAME, TEST_PASSWORD, CAPTCHA_UUID, "wrong");

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(false);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(true);
        when(captchaService.validateCaptcha(CAPTCHA_UUID, "wrong")).thenReturn(false);

        Result<Map<String, Object>> result = userController.login(request, httpRequest);

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("验证码错误"));

        verify(userService, never()).login(anyString(), anyString());
        verify(loginAttemptService, never()).recordFailedAttempt(anyString());
    }

    @Test
    @DisplayName("组合场景 - 首次登录失败 -> 需要验证码 -> 验证码正确但密码错误 -> 锁定")
    void testLogin_FullScenario_MultipleFailuresUntilLock() {
        LoginRequest request = createLoginRequest(TEST_USERNAME, "wrongpass");

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(false);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(false);
        when(userService.login(TEST_USERNAME, "wrongpass")).thenReturn(null);
        when(loginAttemptService.getRemainingAttempts(TEST_USERNAME)).thenReturn(2);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(false);

        Result<Map<String, Object>> result1 = userController.login(request, httpRequest);
        assertTrue(result1.getMessage().contains("剩余2次机会"));

        LoginRequest requestWithCaptcha = createLoginRequestWithCaptcha(
                TEST_USERNAME, "wrongpass", CAPTCHA_UUID, CORRECT_CAPTCHA);
        when(loginAttemptService.requiresCaptcha(TEST_USERNAME)).thenReturn(true);
        when(captchaService.validateCaptcha(CAPTCHA_UUID, CORRECT_CAPTCHA)).thenReturn(true);
        when(loginAttemptService.getRemainingAttempts(TEST_USERNAME)).thenReturn(0);

        Result<Map<String, Object>> result2 = userController.login(requestWithCaptcha, httpRequest);
        assertTrue(result2.getMessage().contains("账户已被锁定"));

        when(loginAttemptService.isLocked(TEST_USERNAME)).thenReturn(true);
        Result<Map<String, Object>> result3 = userController.login(requestWithCaptcha, httpRequest);
        assertTrue(result3.getMessage().contains("账户已被锁定"));
    }

    @Test
    @DisplayName("获取验证码 - 正常返回验证码信息")
    void testGetCaptcha_ShouldReturnUuidAndImage() {
        java.util.Map<String, String> mockCaptcha = new java.util.HashMap<>();
        mockCaptcha.put("uuid", CAPTCHA_UUID);
        mockCaptcha.put("image", "data:image/png;base64,test");
        when(captchaService.generateCaptcha()).thenReturn(mockCaptcha);

        Result<Map<String, String>> result = userController.getCaptcha();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(CAPTCHA_UUID, result.getData().get("uuid"));
        assertNotNull(result.getData().get("image"));
    }

    @Test
    @DisplayName("登出 - 已登录用户登出成功")
    void testLogout_LoggedInUser_ShouldSucceed() {
        User testUser = createTestUser();
        session.setAttribute(SecurityConstants.SESSION_USER_KEY, testUser);
        session.setAttribute(SecurityConstants.USER_SESSION_ID_KEY, "session-123");

        Result<String> result = userController.logout(httpRequest);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("退出成功", result.getData());
        verify(userSessionService).markSessionLogout(anyString());
    }

    @Test
    @DisplayName("登出 - 未登录用户登出也返回成功（幂等）")
    void testLogout_NotLoggedInUser_ShouldReturnSuccess() {
        Result<String> result = userController.logout(httpRequest);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("退出成功", result.getData());
    }
}
