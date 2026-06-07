package com.smart.elderly.enums;

import lombok.Getter;
import java.util.Arrays;
import java.util.List;

@Getter
public enum WarningFollowupTaskStatus {
    PENDING("PENDING", "待处理", "#F56C6C", "danger"),
    IN_PROGRESS("IN_PROGRESS", "处理中", "#E6A23C", "warning"),
    COMPLETED("COMPLETED", "已完成", "#67C23A", "success"),
    OVERDUE("OVERDUE", "已逾期", "#F56C6C", "danger"),
    CANCELLED("CANCELLED", "已取消", "#909399", "info");

    private final String code;
    private final String displayName;
    private final String color;
    private final String tagType;

    WarningFollowupTaskStatus(String code, String displayName, String color, String tagType) {
        this.code = code;
        this.displayName = displayName;
        this.color = color;
        this.tagType = tagType;
    }

    public static WarningFollowupTaskStatus fromCode(String code) {
        if (code == null) {
            return PENDING;
        }
        for (WarningFollowupTaskStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return PENDING;
    }

    public static boolean isValidCode(String code) {
        if (code == null) {
            return false;
        }
        for (WarningFollowupTaskStatus status : values()) {
            if (status.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static WarningFollowupTaskStatus requireValidCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("状态码不能为空");
        }
        for (WarningFollowupTaskStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的状态码: " + code);
    }

    public List<WarningFollowupTaskStatus> getAllowedTransitions() {
        switch (this) {
            case PENDING:
                return Arrays.asList(IN_PROGRESS, COMPLETED, CANCELLED);
            case IN_PROGRESS:
                return Arrays.asList(COMPLETED, CANCELLED, PENDING);
            case COMPLETED:
                return Arrays.asList(PENDING);
            case OVERDUE:
                return Arrays.asList(IN_PROGRESS, COMPLETED, CANCELLED);
            case CANCELLED:
                return Arrays.asList(PENDING);
            default:
                return Arrays.asList();
        }
    }

    public boolean canTransitionTo(WarningFollowupTaskStatus target) {
        return getAllowedTransitions().contains(target);
    }
}
