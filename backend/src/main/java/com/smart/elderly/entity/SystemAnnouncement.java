package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("system_announcements")
public class SystemAnnouncement {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String title;
    
    private String content;
    
    private Integer publisherId;
    
    private String publisherName;
    
    private Boolean isPinned;
    
    private LocalDateTime publishStartTime;
    
    private LocalDateTime publishEndTime;
    
    private String status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @TableField(exist = false)
    private Boolean isRead;
}
