package com.smart.elderly.vo;

import com.smart.elderly.entity.HealthWarningTimeline;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class HealthWarningDetailVO {
    private Integer id;
    private Integer elderlyId;
    private String elderlyName;
    private Integer healthRecordId;
    private String indicatorType;
    private BigDecimal actualValue;
    private BigDecimal thresholdValue;
    private String warningLevel;
    private String warningMessage;
    private String status;
    private String statusName;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;
    private String handledBy;
    private String handleRemark;
    private List<HealthWarningTimeline> timeline;
    private List<String> allowedActions;
}
