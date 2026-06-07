package com.smart.elderly.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class WorkbenchStatsVO {
    private Long totalCount;
    private Long highPriorityCount;
    private Long mediumPriorityCount;
    private Long lowPriorityCount;
    private Long warningCount;
    private Long notificationCount;
    private Long healthRecordCount;
    private Long followupTaskCount;
    private List<Map<String, Object>> elderlyStats;
    private List<WorkbenchItemVO> topPriorityItems;
}
