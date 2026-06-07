package com.smart.elderly.interceptor;

import com.smart.elderly.context.UserContextHolder;
import com.smart.elderly.entity.User;
import com.smart.elderly.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USERNAME_HEADER = "X-Username";

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userIdStr = request.getHeader(USER_ID_HEADER);
        String username = request.getHeader(USERNAME_HEADER);

        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                Integer userId = Integer.parseInt(userIdStr);
                String role = null;
                if (userId != null) {
                    User user = userService.getById(userId);
                    if (user != null) {
                        role = user.getRole();
                    }
                }
                UserContextHolder.setUserInfo(new UserContextHolder.UserInfo(userId, username, role));
            } catch (NumberFormatException e) {
                if (username != null && !username.isEmpty()) {
                    UserContextHolder.setUserInfo(new UserContextHolder.UserInfo(null, username, null));
                }
            }
        } else if (username != null && !username.isEmpty()) {
            UserContextHolder.setUserInfo(new UserContextHolder.UserInfo(null, username, null));
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContextHolder.clear();
    }
}
