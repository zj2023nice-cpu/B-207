package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification_read_records")
public class NotificationReadRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer notificationId;

    private Integer userId;

    private LocalDateTime readAt;
}
