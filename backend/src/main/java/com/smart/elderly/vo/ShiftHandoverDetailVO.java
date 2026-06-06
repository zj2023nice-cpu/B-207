package com.smart.elderly.vo;

import com.smart.elderly.entity.HealthWarningRecord;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShiftHandoverDetailVO {
    private Integer id;
    private String handoverPerson;
    private String takeoverPerson;
    private LocalDateTime handoverTime;
    private String keyElderly;
    private String pendingWarningSummary;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<HealthWarningRecord> relatedWarnings;
}
