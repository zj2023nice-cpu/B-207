package com.smart.elderly.controller;

import com.smart.elderly.annotation.OperationLog;
import com.smart.elderly.common.Result;
import com.smart.elderly.entity.User;
import com.smart.elderly.entity.UserSession;
import com.smart.elderly.service.UserSessionService;
import com.smart.elderly.vo.UserSessionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-sessions")
public class UserSessionController {

    private static final String SESSION_USER_KEY = "LOGIN_USER";

    @Autowired
    private UserSessionService userSessionService;

    @GetMapping("/my-sessions")
    public Result<List<UserSessionVO>> getMySessions(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Result.error("用户未登录");
        }
        User user = (User) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            return Result.error("用户未登录");
        }
        String currentSessionId = session.getId();
        List<UserSessionVO> sessions = userSessionService.getUserSessions(user.getId(), currentSessionId);
        return Result.success(sessions);
    }

    @OperationLog(operation = "使会话失效", description = "用户手动使其他会话失效")
    @PostMapping("/invalidate/{sessionId}")
    public Result<String> invalidateSession(@PathVariable Long sessionId, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Result.error("用户未登录");
        }
        User user = (User) session.getAttribute(SESSION_USER_KEY);
        if (user == null) {
            return Result.error("用户未登录");
        }
        boolean success = userSessionService.invalidateSession(sessionId, user.getId(), user.getId());
        if (success) {
            return Result.success("会话已失效");
        }
        return Result.error("会话不存在或无权限操作");
    }
}
