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

    public static void clear() {
        USER_INFO_THREAD_LOCAL.remove();
    }

    public static class UserInfo {
        private Integer userId;
        private String username;

        public UserInfo(Integer userId, String username) {
            this.userId = userId;
            this.username = username;
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
    }
}
