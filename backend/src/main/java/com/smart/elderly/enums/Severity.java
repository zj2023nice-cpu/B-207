package com.smart.elderly.enums;

import lombok.Getter;

@Getter
public enum Severity {
    INFO("INFO", "提示", 5),
    WARNING("WARNING", "警告", 15),
    CRITICAL("CRITICAL", "严重", 30);

    private final String code;
    private final String description;
    private final int scoreDeduction;

    Severity(String code, String description, int scoreDeduction) {
        this.code = code;
        this.description = description;
        this.scoreDeduction = scoreDeduction;
    }
}
