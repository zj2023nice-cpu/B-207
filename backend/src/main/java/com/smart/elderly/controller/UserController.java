package com.smart.elderly.controller;

import com.smart.elderly.annotation.OperationLog;
import com.smart.elderly.common.Result;
import com.smart.elderly.common.SecurityConstants;
import com.smart.elderly.dto.ChangePasswordDTO;
import com.smart.elderly.dto.LoginRequest;
import com.smart.elderly.dto.UpdateProfileDTO;
import com.smart.elderly.entity.User;
import com.smart.elderly.service.CaptchaService;
import com.smart.elderly.service.LoginAttemptService;
import com.smart.elderly.service.UserService;
import com.smart.elderly.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final String SESSION_USER_KEY = SecurityConstants.SESSION_USER_KEY;

    @Autowired
    private UserService userService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private UserSessionService userSessionService;

    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        Map<String, String> captcha = captchaService.generateCaptcha();
        return Result.success(captcha);
    }

    @OperationLog(operation = "用户登录", description = "用户登录系统")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (loginAttemptService.isLocked(username)) {
            return Result.error("账户已被锁定，请15分钟后再试");
        }

        boolean needsCaptcha = loginAttemptService.requiresCaptcha(username);
        if (needsCaptcha) {
            if (request.getCaptchaUuid() == null || request.getCaptchaCode() == null) {
                return Result.error("请输入验证码");
            }
            boolean captchaValid = captchaService.validateCaptcha(request.getCaptchaUuid(), request.getCaptchaCode());
            if (!captchaValid) {
                return Result.error("验证码错误或已过期");
            }
        }

        User dbUser = userService.login(username, password);
        if (dbUser != null) {
            loginAttemptService.resetAttempts(username);
            
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(SESSION_USER_KEY, dbUser);
            session.setMaxInactiveInterval(SecurityConstants.DEFAULT_SESSION_TIMEOUT_SECONDS);

            userSessionService.recordLoginSession(dbUser, httpRequest);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", dbUser.getId());
            userInfo.put("username", dbUser.getUsername());
            userInfo.put("displayName", dbUser.getDisplayName());
            userInfo.put("phone", dbUser.getPhone());
            userInfo.put("role", dbUser.getRole());
            userInfo.put("lastLoginTime", dbUser.getLastLoginTime());
            
            return Result.success(userInfo);
        }

        loginAttemptService.recordFailedAttempt(username);
        int remaining = loginAttemptService.getRemainingAttempts(username);

        if (remaining > 0) {
            if (loginAttemptService.requiresCaptcha(username)) {
                return Result.error("用户名或密码错误，剩余" + remaining + "次机会，请输入验证码");
            }
            return Result.error("用户名或密码错误，剩余" + remaining + "次机会");
        } else {
            return Result.error("登录失败次数过多，账户已被锁定15分钟");
        }
    }

    @GetMapping("/current")
    public Result<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Result.error("用户未登录");
        }
        User user = (User) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            return Result.error("用户未登录");
        }
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("displayName", user.getDisplayName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("role", user.getRole());
        userInfo.put("lastLoginTime", user.getLastLoginTime());
        return Result.success(userInfo);
    }

    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Result.error("用户未登录");
        }
        User user = (User) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            return Result.error("用户未登录");
        }
        Map<String, Object> profile = userService.getProfile(user.getId());
        if (profile == null) {
            return Result.error("用户不存在");
        }
        return Result.success(profile);
    }

    @OperationLog(operation = "更新个人资料", description = "用户更新个人资料")
    @PutMapping("/profile")
    public Result<String> updateProfile(@Valid @RequestBody UpdateProfileDTO dto, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Result.error("用户未登录");
        }
        User user = (User) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            return Result.error("用户未登录");
        }
        boolean success = userService.updateProfile(user.getId(), dto);
        if (!success) {
            return Result.error("更新失败");
        }
        User updatedUser = userService.getById(user.getId());
        session.setAttribute(SESSION_USER_KEY, updatedUser);
        return Result.success("资料更新成功");
    }

    @OperationLog(operation = "修改密码", description = "用户修改登录密码")
    @PutMapping("/password")
    public Result<String> changePassword(@Valid @RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Result.error("用户未登录");
        }
        User user = (User) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            return Result.error("用户未登录");
        }
        String errorMsg = userService.changePassword(user.getId(), dto);
        if (errorMsg != null) {
            return Result.error(errorMsg);
        }
        String sessionId = session.getId();
        userSessionService.markSessionLogout(sessionId);
        session.invalidate();
        return Result.success("密码修改成功，请重新登录");
    }

    @OperationLog(operation = "用户登出", description = "用户退出登录")
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionId = session.getId();
            userSessionService.markSessionLogout(sessionId);
            session.invalidate();
        }
        return Result.success("退出成功");
    }

    @OperationLog(operation = "用户注册", description = "用户注册新账号")
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody User user) {
        User existingUser = userService
                .getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, user.getUsername()));
        if (existingUser != null) {
            return Result.error("该用户名已被注册");
        }
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("user");
        }
        userService.register(user);
        return Result.success("注册成功");
    }
}
