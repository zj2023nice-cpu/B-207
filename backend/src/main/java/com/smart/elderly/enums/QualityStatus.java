package com.smart.elderly.enums;

import lombok.Getter;

@Getter
public enum QualityStatus {
    NORMAL("NORMAL", "正常"),
    SUSPICIOUS("SUSPICIOUS", "待复核"),
    REVIEWED("REVIEWED", "已复核"),
    IGNORED("IGNORED", "已忽略");

    private final String code;
    private final String description;

    QualityStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
