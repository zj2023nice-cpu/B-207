package com.smart.elderly.controller;

import com.smart.elderly.annotation.OperationLog;
import com.smart.elderly.common.Result;
import com.smart.elderly.dto.LoginRequest;
import com.smart.elderly.entity.User;
import com.smart.elderly.service.CaptchaService;
import com.smart.elderly.service.LoginAttemptService;
import com.smart.elderly.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        Map<String, String> captcha = captchaService.generateCaptcha();
        return Result.success(captcha);
    }

    @OperationLog(operation = "用户登录", description = "用户登录系统")
    @PostMapping("/login")
    public Result<User> login(@Valid @RequestBody LoginRequest request) {
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
            dbUser.setPassword(null);
            return Result.success(dbUser);
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

    @OperationLog(operation = "用户注册", description = "用户注册新账号")
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody User user) {
        User existingUser = userService
                .getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, user.getUsername()));
        if (existingUser != null) {
            return Result.error("该用户名已被注册");
        }
        userService.register(user);
        return Result.success("注册成功");
    }
}
