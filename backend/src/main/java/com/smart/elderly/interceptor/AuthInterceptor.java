package com.smart.elderly.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.elderly.common.Result;
import com.smart.elderly.common.SecurityConstants;
import com.smart.elderly.entity.User;
import com.smart.elderly.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String SESSION_USER_KEY = SecurityConstants.SESSION_USER_KEY;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserSessionService userSessionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(SESSION_USER_KEY);
            if (user != null) {
                String sessionId = session.getId();
                if (!userSessionService.isSessionValid(sessionId)) {
                    session.invalidate();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    PrintWriter writer = response.getWriter();
                    writer.write(objectMapper.writeValueAsString(Result.error("会话已失效，请重新登录")));
                    writer.flush();
                    writer.close();
                    return false;
                }
                userSessionService.updateLastActiveTime(sessionId);
                return true;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(Result.error("用户未登录或登录已过期")));
        writer.flush();
        writer.close();
        return false;
    }
}
