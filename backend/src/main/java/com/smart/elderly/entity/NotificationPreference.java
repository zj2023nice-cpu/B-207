package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification_preference")
public class NotificationPreference {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer userId;
    
    private String enabledTypes;
    
    private String highPriorityTypes;
    
    private Boolean doNotDisturbEnabled;
    
    private String doNotDisturbStart;
    
    private String doNotDisturbEnd;
    
    private Boolean showBadgeInDnd;
    
    private Boolean soundEnabled;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
