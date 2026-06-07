package com.smart.elderly.common;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String SESSION_USER_KEY = "LOGIN_USER";
    public static final String USER_SESSION_ID_KEY = "USER_SESSION_ID";

    public static final int DEFAULT_MAX_LOGIN_ATTEMPTS = 3;
    public static final int DEFAULT_CAPTCHA_THRESHOLD = 2;
    public static final long DEFAULT_LOCK_DURATION_MINUTES = 15;
    public static final long DEFAULT_ATTEMPT_EXPIRE_MINUTES = 30;

    public static final String LOGIN_ATTEMPT_PREFIX = "login:attempt:";
    public static final String LOGIN_LOCK_PREFIX = "login:lock:";

    public static final int DEFAULT_SESSION_TIMEOUT_SECONDS = 3600 * 24;
    public static final int DEFAULT_SESSION_EXPIRE_HOURS = 24;

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";
}
