package com.smart.elderly.context;

public class UserContextHolder {

    private static final ThreadLocal<UserInfo> USER_INFO_THREAD_LOCAL = new ThreadLocal<>();

    public static void setUserInfo(UserInfo userInfo) {
        USER_INFO_THREAD_LOCAL.set(userInfo);
    }

    public static UserInfo getUserInfo() {
        return USER_INFO_THREAD_LOCAL.get();
    }

    public static Integer getUserId() {
        UserInfo userInfo = USER_INFO_THREAD_LOCAL.get();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    public static String getUsername() {
        UserInfo userInfo = USER_INFO_THREAD_LOCAL.get();
        return userInfo != null ? userInfo.getUsername() : null;
    }

    public static String getRole() {
        UserInfo userInfo = USER_INFO_THREAD_LOCAL.get();
        return userInfo != null ? userInfo.getRole() : null;
    }

    public static boolean isAdmin() {
        String role = getRole();
        return "admin".equalsIgnoreCase(role);
    }

    public static void clear() {
        USER_INFO_THREAD_LOCAL.remove();
    }

    public static class UserInfo {
        private Integer userId;
        private String username;
        private String role;

        public UserInfo(Integer userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
