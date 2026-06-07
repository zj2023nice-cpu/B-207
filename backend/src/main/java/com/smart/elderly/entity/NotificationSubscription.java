package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification_subscription")
public class NotificationSubscription {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer userId;
    
    private Boolean enabled;
    
    private String notificationTypes;
    
    private Boolean onlyAbnormal;
    
    private Boolean onlyFollowedElderly;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
