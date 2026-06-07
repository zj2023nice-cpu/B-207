package com.smart.elderly.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FollowedElderlyVO {
    private Integer elderlyId;
    private String name;
    private Integer age;
    private String gender;
    private String phone;
    private String address;
    private String status;
    
    private LocalDateTime lastCheckTime;
    private String lastHealthSummary;
    private Boolean lastIsAbnormal;
    
    private Integer pendingWarningCount;
    private Integer unreadNotificationCount;
    
    private LocalDateTime followTime;
}
