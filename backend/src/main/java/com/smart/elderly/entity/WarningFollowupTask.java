package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("warning_followup_tasks")
public class WarningFollowupTask {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer warningRecordId;

    private Integer elderlyId;

    private String title;

    private String description;

    private Integer assigneeId;

    private String assigneeName;

    private LocalDateTime deadline;

    private String priority;

    private String status;

    private String remark;

    private Integer createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    private Boolean reminder24hSent;

    private Boolean reminder1hSent;

    private Boolean overdueReminderSent;

    @TableField(exist = false)
    private String elderlyName;

    @TableField(exist = false)
    private String warningMessage;

    @TableField(exist = false)
    private String warningIndicatorType;
}
