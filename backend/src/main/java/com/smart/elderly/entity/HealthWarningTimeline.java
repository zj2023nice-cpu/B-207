package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("health_warning_timeline")
public class HealthWarningTimeline {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer warningRecordId;
    
    private String actionType;
    
    private String fromStatus;
    
    private String toStatus;
    
    private String operator;
    
    private String remark;
    
    private LocalDateTime createdAt;
}
