package com.smart.elderly.enums;

import lombok.Getter;
import java.util.Arrays;
import java.util.List;

@Getter
public enum UserSessionStatus {
    ACTIVE("ACTIVE", "活跃", "#67C23A", "success"),
    INVALIDATED("INVALIDATED", "已失效", "#909399", "info"),
    LOGOUT("LOGOUT", "已登出", "#E6A23C", "warning"),
    EXPIRED("EXPIRED", "已过期", "#F56C6C", "danger");

    private final String code;
    private final String displayName;
    private final String color;
    private final String tagType;

    UserSessionStatus(String code, String displayName, String color, String tagType) {
        this.code = code;
        this.displayName = displayName;
        this.color = color;
        this.tagType = tagType;
    }

    public static UserSessionStatus fromCode(String code) {
        if (code == null) {
            return ACTIVE;
        }
        for (UserSessionStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return ACTIVE;
    }

    public static boolean isValidCode(String code) {
        if (code == null) {
            return false;
        }
        for (UserSessionStatus status : values()) {
            if (status.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static UserSessionStatus requireValidCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("会话状态不能为空");
        }
        for (UserSessionStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的会话状态: " + code);
    }

    public List<UserSessionStatus> getAllowedTransitions() {
        switch (this) {
            case ACTIVE:
                return Arrays.asList(INVALIDATED, LOGOUT, EXPIRED);
            case INVALIDATED:
                return Arrays.asList();
            case LOGOUT:
                return Arrays.asList();
            case EXPIRED:
                return Arrays.asList();
            default:
                return Arrays.asList();
        }
    }

    public boolean canTransitionTo(UserSessionStatus target) {
        return getAllowedTransitions().contains(target);
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
