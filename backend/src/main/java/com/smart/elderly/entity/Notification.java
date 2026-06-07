package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notifications")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer userId;
    
    private Integer elderlyId;
    
    private Integer warningRecordId;
    
    private String title;
    
    private String content;
    
    private String notificationType;
    
    private String status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime readAt;
    
    private Boolean invalidated;
    private LocalDateTime invalidatedAt;
    private String invalidatedReason;
    private Integer invalidatedByCorrectionId;

    @TableField(exist = false)
    private String elderlyName;
}
