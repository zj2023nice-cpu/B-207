package com.smart.elderly.enums;

import lombok.Getter;
import java.util.Arrays;
import java.util.List;

@Getter
public enum HealthWarningStatus {
    PENDING("PENDING", "待处理", "#F56C6C", "danger"),
    READ("READ", "已读", "#E6A23C", "warning"),
    HANDLED("HANDLED", "已处理", "#67C23A", "success"),
    IGNORED("IGNORED", "已忽略", "#909399", "info"),
    REOPENED("REOPENED", "重新打开", "#F56C6C", "danger"),
    ESCALATED("ESCALATED", "升级处理", "#E6A23C", "warning");

    public static final String INVALIDATED_CODE = "INVALIDATED";

    private final String code;
    private final String displayName;
    private final String color;
    private final String tagType;

    HealthWarningStatus(String code, String displayName, String color, String tagType) {
        this.code = code;
        this.displayName = displayName;
        this.color = color;
        this.tagType = tagType;
    }

    public static HealthWarningStatus fromCode(String code) {
        if (code == null) {
            return PENDING;
        }
        for (HealthWarningStatus status : values()) {
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
        for (HealthWarningStatus status : values()) {
            if (status.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static HealthWarningStatus requireValidCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("状态码不能为空");
        }
        for (HealthWarningStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的状态码: " + code);
    }

    public List<HealthWarningStatus> getAllowedTransitions() {
        switch (this) {
            case PENDING:
                return Arrays.asList(READ, HANDLED, IGNORED, ESCALATED);
            case READ:
                return Arrays.asList(HANDLED, IGNORED, ESCALATED, PENDING);
            case HANDLED:
                return Arrays.asList(REOPENED);
            case IGNORED:
                return Arrays.asList(REOPENED);
            case REOPENED:
                return Arrays.asList(READ, HANDLED, IGNORED, ESCALATED);
            case ESCALATED:
                return Arrays.asList(HANDLED, READ);
            default:
                return Arrays.asList();
        }
    }

    public boolean canTransitionTo(HealthWarningStatus target) {
        return getAllowedTransitions().contains(target);
    }

    public static List<String> getPendingStatusCodes() {
        return Arrays.asList(PENDING.getCode(), REOPENED.getCode(), ESCALATED.getCode(), READ.getCode());
    }

    public static List<HealthWarningStatus> getPendingStatuses() {
        return Arrays.asList(PENDING, REOPENED, ESCALATED, READ);
    }

    public boolean isPending() {
        return this == PENDING || this == REOPENED || this == ESCALATED || this == READ;
    }

    public boolean isClosed() {
        return this == HANDLED || this == IGNORED;
    }

    public String getActionType() {
        switch (this) {
            case READ:
                return "READ";
            case HANDLED:
                return "HANDLE";
            case IGNORED:
                return "IGNORE";
            case REOPENED:
                return "REOPEN";
            case ESCALATED:
                return "ESCALATE";
            default:
                return "UPDATE";
        }
    }
}
