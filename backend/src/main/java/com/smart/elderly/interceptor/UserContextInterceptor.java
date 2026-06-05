package com.smart.elderly.interceptor;

import com.smart.elderly.context.UserContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USERNAME_HEADER = "X-Username";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userIdStr = request.getHeader(USER_ID_HEADER);
        String username = request.getHeader(USERNAME_HEADER);

        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                Integer userId = Integer.parseInt(userIdStr);
                UserContextHolder.setUserInfo(new UserContextHolder.UserInfo(userId, username));
            } catch (NumberFormatException e) {
                if (username != null && !username.isEmpty()) {
                    UserContextHolder.setUserInfo(new UserContextHolder.UserInfo(null, username));
                }
            }
        } else if (username != null && !username.isEmpty()) {
            UserContextHolder.setUserInfo(new UserContextHolder.UserInfo(null, username));
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContextHolder.clear();
    }
}
