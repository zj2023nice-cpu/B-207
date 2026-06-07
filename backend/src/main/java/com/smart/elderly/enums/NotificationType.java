package com.smart.elderly.enums;

import lombok.Getter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum NotificationType {
    HEALTH_WARNING("HEALTH_WARNING", "健康预警", true),
    FOLLOWUP_TASK("FOLLOWUP_TASK", "跟进任务", true),
    FOLLOWUP_TASK_REMINDER("FOLLOWUP_TASK_REMINDER", "任务提醒", true),
    SYSTEM_ANNOUNCEMENT("SYSTEM_ANNOUNCEMENT", "系统公告", false),
    QUALITY_REVIEW("QUALITY_REVIEW", "质量评审", false),
    GENERAL("GENERAL", "通用通知", false);

    private final String code;
    private final String displayName;
    private final boolean highPriorityByDefault;

    NotificationType(String code, String displayName, boolean highPriorityByDefault) {
        this.code = code;
        this.displayName = displayName;
        this.highPriorityByDefault = highPriorityByDefault;
    }

    public static NotificationType fromCode(String code) {
        if (code == null) {
            return GENERAL;
        }
        for (NotificationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return GENERAL;
    }

    public static boolean isValidCode(String code) {
        if (code == null) {
            return false;
        }
        for (NotificationType type : values()) {
            if (type.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static NotificationType requireValidCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("通知类型不能为空");
        }
        for (NotificationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的通知类型: " + code);
    }

    public static List<String> getDefaultHighPriorityTypes() {
        return Arrays.stream(values())
                .filter(NotificationType::isHighPriorityByDefault)
                .map(NotificationType::getCode)
                .collect(Collectors.toList());
    }
}
