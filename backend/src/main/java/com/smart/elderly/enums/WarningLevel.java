package com.smart.elderly.enums;

import lombok.Getter;

@Getter
public enum WarningLevel {
    HIGH("HIGH", "严重", 3),
    MEDIUM("MEDIUM", "中等", 2),
    LOW("LOW", "轻微", 1);

    private final String code;
    private final String displayName;
    private final int score;

    WarningLevel(String code, String displayName, int score) {
        this.code = code;
        this.displayName = displayName;
        this.score = score;
    }

    public static WarningLevel fromCode(String code) {
        if (code == null) {
            return MEDIUM;
        }
        for (WarningLevel level : values()) {
            if (level.code.equalsIgnoreCase(code)) {
                return level;
            }
        }
        // 兼容中文别名
        for (WarningLevel level : values()) {
            if (level.displayName.equals(code)) {
                return level;
            }
        }
        return MEDIUM;
    }

    public static boolean isValidCode(String code) {
        if (code == null) {
            return false;
        }
        for (WarningLevel level : values()) {
            if (level.code.equalsIgnoreCase(code) || level.displayName.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
