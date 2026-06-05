package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("operation_logs")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String username;
    private String operation;
    private String method;
    private String params;
    private LocalDateTime operationTime;
    private String ip;
    private Boolean success;
    private String errorMsg;
    private String description;
}
