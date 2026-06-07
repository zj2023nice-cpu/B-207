package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("export_task")
public class ExportTask {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String taskName;
    private String exportType;
    private String exportParams;
    private String exportRangeDesc;
    private String status;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private Integer totalCount;
    private Integer retryCount;
    private Integer maxRetry;
    private String errorMessage;
    private Integer createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
