package com.smart.elderly.interceptor;

import com.smart.elderly.common.SecurityConstants;
import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final String SESSION_USER_KEY = SecurityConstants.SESSION_USER_KEY;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(SESSION_USER_KEY);
            if (user != null) {
                UserContextHolder.setUserInfo(
                    new UserContextHolder.UserInfo(user.getId(), user.getUsername(), user.getRole())
                );
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContextHolder.clear();
    }
}
