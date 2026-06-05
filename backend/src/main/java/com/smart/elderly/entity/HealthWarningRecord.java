package com.smart.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("health_warning_records")
public class HealthWarningRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer elderlyId;
    
    private Integer healthRecordId;
    
    private String indicatorType;
    
    private BigDecimal actualValue;
    
    private BigDecimal thresholdValue;
    
    private String warningLevel;
    
    private String warningMessage;
    
    private String status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime handledAt;
    
    private String handledBy;
    
    private String handleRemark;

    @TableField(exist = false)
    private String elderlyName;
}
