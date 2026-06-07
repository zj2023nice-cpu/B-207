package com.smart.elderly.vo;

import lombok.Data;

@Data
public class HealthWarningTransitionVO {
    private String targetStatus;
    private String operator;
    private String remark;
}
