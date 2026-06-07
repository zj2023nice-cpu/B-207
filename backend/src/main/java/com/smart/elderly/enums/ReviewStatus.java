package com.smart.elderly.enums;

import lombok.Getter;

@Getter
public enum ReviewStatus {
    PENDING("PENDING", "待复核"),
    APPROVED("APPROVED", "确认有效"),
    CORRECTED("CORRECTED", "已更正"),
    IGNORED("IGNORED", "忽略");

    private final String code;
    private final String description;

    ReviewStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
