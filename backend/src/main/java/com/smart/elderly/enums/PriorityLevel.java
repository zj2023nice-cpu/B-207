package com.smart.elderly.enums;

import lombok.Getter;

@Getter
public enum PriorityLevel {
    HIGH("HIGH", "高", 3, "#F56C6C", "danger"),
    MEDIUM("MEDIUM", "中", 2, "#E6A23C", "warning"),
    LOW("LOW", "低", 1, "#909399", "info");

    private final String code;
    private final String displayName;
    private final int score;
    private final String color;
    private final String tagType;

    PriorityLevel(String code, String displayName, int score, String color, String tagType) {
        this.code = code;
        this.displayName = displayName;
        this.score = score;
        this.color = color;
        this.tagType = tagType;
    }

    public static PriorityLevel fromCode(String code) {
        if (code == null) {
            return MEDIUM;
        }
        for (PriorityLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        return MEDIUM;
    }

    public static boolean isValidCode(String code) {
        if (code == null) {
            return false;
        }
        for (PriorityLevel level : values()) {
            if (level.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static PriorityLevel requireValidCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("优先级不能为空");
        }
        for (PriorityLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("未知的优先级: " + code);
    }

    public static PriorityLevel fromScore(int totalScore, int highThreshold, int mediumThreshold) {
        if (totalScore >= highThreshold) {
            return HIGH;
        }
        if (totalScore >= mediumThreshold) {
            return MEDIUM;
        }
        return LOW;
    }
}
