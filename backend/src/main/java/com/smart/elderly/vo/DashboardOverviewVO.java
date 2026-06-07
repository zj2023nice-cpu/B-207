package com.smart.elderly.vo;

import com.smart.elderly.entity.OperationLog;
import lombok.Data;
import java.util.List;

@Data
public class DashboardOverviewVO {
    private Long elderlyTotal;
    private Long todayHealthRecords;
    private Long recentAbnormalElderly;
    private Long pendingWarnings;
    private Long unreadNotifications;
    private List<OperationLog> recentActivities;
}
