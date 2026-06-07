package com.smart.elderly.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExportTaskVO {
    private Integer id;
    private String taskName;
    private String exportType;
    private String exportTypeDesc;
    private String exportRangeDesc;
    private String status;
    private String statusDesc;
    private String fileName;
    private Long fileSize;
    private Integer totalCount;
    private Integer retryCount;
    private String errorMessage;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Boolean canDownload;
    private Boolean canRetry;
}
