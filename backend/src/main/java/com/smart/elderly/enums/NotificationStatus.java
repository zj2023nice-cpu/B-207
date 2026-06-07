package com.smart.elderly.enums;

import lombok.Getter;
import java.util.Arrays;
import java.util.List;

@Getter
public enum NotificationStatus {
    UNREAD("UNREAD", "未读", "#F56C6C", "danger"),
    READ("READ", "已读", "#909399", "info");

    private final String code;
    private final String displayName;
    private final String color;
    private final String tagType;

    NotificationStatus(String code, String displayName, String color, String tagType) {
        this.code = code;
        this.displayName = displayName;
        this.color = color;
        this.tagType = tagType;
    }

    public static NotificationStatus fromCode(String code) {
        if (code == null) {
            return UNREAD;
        }
        for (NotificationStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return UNREAD;
    }

    public static boolean isValidCode(String code) {
        if (code == null) {
            return false;
        }
        for (NotificationStatus status : values()) {
            if (status.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static NotificationStatus requireValidCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("通知状态不能为空");
        }
        for (NotificationStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的通知状态: " + code);
    }

    public List<NotificationStatus> getAllowedTransitions() {
        switch (this) {
            case UNREAD:
                return Arrays.asList(READ);
            case READ:
                return Arrays.asList(UNREAD);
            default:
                return Arrays.asList();
        }
    }

    public boolean canTransitionTo(NotificationStatus target) {
        return getAllowedTransitions().contains(target);
    }
}
