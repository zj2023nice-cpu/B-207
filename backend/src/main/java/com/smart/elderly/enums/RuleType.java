package com.smart.elderly.enums;

import lombok.Getter;

@Getter
public enum RuleType {
    MISSING("MISSING", "缺失检测"),
    EXTREME("EXTREME", "极端值检测"),
    DUPLICATE("DUPLICATE", "重复检测"),
    CONTRADICTION("CONTRADICTION", "逻辑矛盾");

    private final String code;
    private final String description;

    RuleType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
